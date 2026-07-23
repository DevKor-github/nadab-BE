package com.devkor.ifive.nadab.domain.pdfexport.application.render;

import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportType;
import com.devkor.ifive.nadab.domain.pdfexport.support.PdfSyntheticData;
import com.devkor.ifive.nadab.global.core.pdf.PdfAssetLoader;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 1 — PDF 렌더 실측 하네스. DB·Spring·시크릿 0. 순수 JVM 에서 프로덕션 렌더 경로(assemble → render)를 태워
 * peak heap·시간·CPU·공존을 잰다. (package-private 협력자 접근 위해 PdfPreviewTest 와 같은 패키지.)
 */
@Tag("bench")
class PdfExportRenderBenchmark {

    private static final int DAYS = Integer.getInteger("pdf.bench.days", 366);
    private static final double PHOTO_RATIO = Double.parseDouble(System.getProperty("pdf.bench.photoRatio", "1.0"));
    private static final LocalDate END = LocalDate.parse(System.getProperty("pdf.bench.endDate", "2026-07-23"));
    /** 2개 동시 렌더(CPU 경합) 실행 여부. 힙 탐색 시 -Dpdf.bench.concurrent=false 로 단일만. */
    private static final boolean RUN_CONCURRENT = Boolean.parseBoolean(System.getProperty("pdf.bench.concurrent", "true"));
    /** 사진 수 스윕(days=366 고정, 사진 수만 증가). 예: -Dpdf.bench.sweepPhotos=0,100,200,366 */
    private static final String SWEEP_PHOTOS = System.getProperty("pdf.bench.sweepPhotos");
    /** 기간(일수) 스윕(사진 100% 고정, 일수만 증가). 예: -Dpdf.bench.sweepDays=90,180,366 */
    private static final String SWEEP_DAYS = System.getProperty("pdf.bench.sweepDays");
    /** 배경 점유 N MB 를 live 로 고정(동시 워크로드 모사) 후 렌더 측정. 예: -Dpdf.bench.backgroundMB=100 */
    private static final int BACKGROUND_MB = Integer.getInteger("pdf.bench.backgroundMB", 0);
    /** 단일 렌더 결과 PDF 를 이 경로에 write(시각 눈검토용, 측정 X). */
    private static final String DUMP_PDF = System.getProperty("pdf.bench.dumpPdf");

    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    /** 배경 점유 블록(동시 워크로드 모사). 인스턴스 필드라 테스트 내내 live → 렌더 측정 동안 heap 을 실제로 차지한다. */
    private final List<byte[]> background = new ArrayList<>();

    @Test
    void 일년치_단일렌더_실측_3종() throws Exception {
        Pipeline pipe = Pipeline.production();

        line();
        System.out.println("[PDF BENCH] Phase 1 — 렌더 실측 3종");
        line();
        printEnvironment();

        // ── 사진 프리플라이트: 측정 전에 모든 원본 변이를 실제로 디코드+640재인코딩해 본다. ──
        //    실패(빈 사진)면 여기서 즉시 터트려, "사진 없는 렌더"를 조용히 측정하는 사고를 막는다.
        preflightPhotos();

        // ── 배경 점유(동시 워크로드 모사): 렌더 전에 N MB 를 pinned. 이후 모든 렌더는 이 위에서 돎. ──
        allocateBackground();

        // ── 스윕 모드(export 상한 사이징): 단일 상세 측정 대신 곡선을 뽑는다. 기간 스윕 우선. ──
        if (SWEEP_DAYS != null && !SWEEP_DAYS.isBlank()) {
            runDaysSweep(pipe);
            return;
        }
        if (SWEEP_PHOTOS != null && !SWEEP_PHOTOS.isBlank()) {
            runPhotoSweep(pipe);
            return;
        }

        PdfSyntheticData data = PdfSyntheticData.of(END, DAYS, PHOTO_RATIO); // 기본 photoRatio=1.0 = 최악(사진 100%)

        // ── 워밍업: JIT·metaspace·code cache 안정화(작은 규모 1회, 측정값 오염 방지) ──
        System.out.println("\n[워밍업] 소규모 렌더 1회(측정 제외)…");
        PdfSyntheticData warm = PdfSyntheticData.of(END, Math.min(14, DAYS), PHOTO_RATIO);
        assertThat(pipe.render(PdfExportType.REPORT_AND_ANSWER, warm)).hasSizeGreaterThan(1000);
        warm = null; // 워밍업 데이터가 1년치 측정 중 힙에 남지 않도록(합성 모드의 변이 PNG 오염 제거)
        settle();

        // ── ② baseline(렌더 전 안정 상태) ──
        Snapshot.capture("baseline(렌더 전, gc 후)").print();

        // ── ① peak heap + 단계별 시간(단일 1년치 렌더) ──
        System.out.println("\n[① 단일 1년치 렌더 — peak heap · 단계별 시간]");
        resetHeapPoolPeaks();
        HeapSampler sampler = HeapSampler.start(memoryBean);

        long cpuBefore = processCpuTimeNanos();
        long t0 = System.nanoTime();
        PdfHtmlAssembler.AssembledDocument doc = pipe.assemble(PdfExportType.REPORT_AND_ANSWER, data);
        long t1 = System.nanoTime();
        byte[] pdf = pipe.renderer.render(doc.xhtml(), doc.inlineAssets());
        long t2 = System.nanoTime();
        long cpuAfter = processCpuTimeNanos();

        sampler.stop();
        long heapPoolPeak = heapPoolPeakBytes();

        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).startsWith("%PDF-");
        dumpPdfIfRequested(pdf);

        System.out.printf("  답변 %d · 주간 %d · 월간V2 %d · 사진 %d장%n",
                data.answers().size(), data.weeklies().size(), data.monthlyV2s().size(),
                data.answers().stream().filter(a -> a.imageKey() != null).count());
        System.out.printf("  assemble(사진 디코드+640재인코딩 포함) : %,d ms%n", ms(t1 - t0));
        System.out.printf("  render(openhtmltopdf → PDF)          : %,d ms%n", ms(t2 - t1));
        System.out.printf("  전체 렌더 wall                        : %,d ms%n", ms(t2 - t0));
        System.out.printf("  XHTML 길이                            : %,d chars (사진 asset: 서빙이라 XHTML 엔 base64 없음 — 텍스트/토큰만)%n", doc.xhtml().length());
        System.out.printf("  사진 asset 수(= 인라인 맵 크기)         : %,d장 (raw JPEG 바이트로 상주, base64/UTF-16 인플레이션 없음)%n", doc.inlineAssets().size());
        System.out.printf("  결과 PDF 크기                         : %,d bytes (%,d KB)%n", pdf.length, pdf.length / 1024);
        System.out.printf("  ★ peak heap(샘플러 max, ★신뢰지표)      : %,d bytes (%,d MB)%n", sampler.maxUsed(), sampler.maxUsed() / (1024 * 1024));
        System.out.printf("  peak heap(풀별 peak 합산, ⚠과다계상)    : %,d bytes (%,d MB) — Eden/Survivor/Old 의 서로 다른 시점 peak 합이라 -Xmx 초과 가능(참고만)%n",
                heapPoolPeak, heapPoolPeak / (1024 * 1024));

        // xhtml·pdf·data 를 여전히 붙든 채 gc → 렌더 종료 시점의 live-ish 유지량(peak 의 미수집 쓰레기 제외한 하한).
        settle();
        long settledUsed = memoryBean.getHeapMemoryUsage().getUsed();
        System.out.printf("  post-render 유지(gc 후 live-ish)         : %,d MB  ← 완주 필요 힙은 (이 값 ~ peak) 사이, 결정값은 -PbenchXmx 탐색%n",
                settledUsed / (1024 * 1024));

        // ── ③ CPU(단일) ──
        double wallSec = (t2 - t0) / 1e9;
        double cpuSec = (cpuAfter - cpuBefore) / 1e9;
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("\n[③ CPU — 단일 렌더]");
        System.out.printf("  process CPU time(렌더 구간)            : %.2f s%n", cpuSec);
        System.out.printf("  wall                                  : %.2f s%n", wallSec);
        System.out.printf("  평균 코어 사용(=CPU/ wall)             : %.2f cores (가용 %d)%n", cpuSec / wallSec, cores);

        // ── ③ 2vCPU 경합: 2개 동시 렌더 wall 팽창(힙 탐색 시 -Dpdf.bench.concurrent=false 로 스킵) ──
        if (RUN_CONCURRENT) {
            System.out.println("\n[③ 2개 동시 렌더 — 2vCPU 경합 관측]");
            measureConcurrent(pipe, 2);
        } else {
            System.out.println("\n[③ 2개 동시 렌더 — 스킵(-Dpdf.bench.concurrent=false)]");
        }

        line();
        System.out.println("[판정 가이드 — 지표별 이식성(★중요)]");
        System.out.println("  ① peak heap = CPU 무관·이식성 O. 힙은 데이터+코드가 정함(머신 속도 아님).");
        System.out.println("     · prod 추정 힙 ~512MB(=-Xmx 미설정 → MaxRAMPercentage 25%)와 비교. peak ≳ 512MB → max1 도 위험.");
        System.out.println("     · 단 이 peak 엔 미수집 쓰레기 포함(2g라 GC 지연) → 부풀 수 있음. ★결정적 답 = \"완주하는 최소 -Xmx\":");
        System.out.println("       ./gradlew pdfBench -PbenchXmx=512m -Dpdf.bench.concurrent=false (640m/512m/448m… 낮춰 OOM 지점 탐색).");
        System.out.println("  ③ 렌더 시간·CPU = 이식성 X(이 PC 는 dev급이라 낙관적). 절대 시간 믿지 말 것 — 버전간 상대 델타로만.");
        System.out.printf("     · 지금 availableProcessors=%d. 다코어면 2개 동시 경합이 안 보임 → t3.small 재현: -PbenchCpu=2%n",
                Runtime.getRuntime().availableProcessors());
        System.out.println("  ② baseline/free RAM = 순수 JVM 벤치(Spring 미기동)라 prod baseline 아님. 실서버 jcmd VM.flags·actuator 로 별도 확인.");
        line();
    }

    /* ────────────────────────── 렌더 파이프라인(협력자 실객체) ────────────────────────── */

    private static final class Pipeline {
        final PdfHtmlAssembler assembler;
        final PdfRenderer renderer;

        private Pipeline(PdfHtmlAssembler assembler, PdfRenderer renderer) {
            this.assembler = assembler;
            this.renderer = renderer;
        }

        static Pipeline production() throws Exception {
            PdfAssetLoader assets = new PdfAssetLoader();
            invoke(assets, "load");
            PdfShadowRenderer shadow = new PdfShadowRenderer();
            PdfHtmlAssembler assembler = new PdfHtmlAssembler(assets, new EmotionRadarChartRenderer(), shadow);
            invoke(assembler, "loadCss");
            return new Pipeline(assembler, new PdfRenderer(assets, shadow));
        }

        PdfHtmlAssembler.AssembledDocument assemble(PdfExportType type, PdfSyntheticData d) {
            return assembler.assemble(type, d.answers(), d.weeklies(), d.monthlies(), d.monthlyV2s(), d.photoResolver());
        }

        byte[] render(PdfExportType type, PdfSyntheticData d) {
            PdfHtmlAssembler.AssembledDocument doc = assemble(type, d);
            return renderer.render(doc.xhtml(), doc.inlineAssets());
        }
    }

    /* ────────────────────────── 측정 유틸 ────────────────────────── */

    /** 백그라운드 폴링으로 힙 used max 를 추적(풀 peak 과 교차검증). */
    private static final class HeapSampler {
        private final Thread thread;
        private final AtomicLong max = new AtomicLong(0);
        private volatile boolean running = true;

        private HeapSampler(MemoryMXBean bean) {
            this.thread = new Thread(() -> {
                while (running) {
                    long used = bean.getHeapMemoryUsage().getUsed();
                    max.accumulateAndGet(used, Math::max);
                    try { Thread.sleep(0, 500_000); } catch (InterruptedException e) { return; }
                }
            }, "heap-sampler");
            this.thread.setDaemon(true);
        }

        static HeapSampler start(MemoryMXBean bean) {
            HeapSampler s = new HeapSampler(bean);
            s.thread.start();
            return s;
        }

        void stop() throws InterruptedException {
            running = false;
            thread.join(1000);
        }

        long maxUsed() { return max.get(); }
    }

    private record Snapshot(String label, long heapUsed, long nonHeapUsed, long metaspace,
                            long codeCache, long directBuffer, long freeRam, long totalRam) {
        static Snapshot capture(String label) {
            settle();
            MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
            long meta = poolUsed("Metaspace");
            long code = poolUsed("CodeCache") + poolUsed("CodeHeap");
            long direct = 0;
            for (BufferPoolMXBean b : ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class)) {
                if ("direct".equals(b.getName())) direct = b.getMemoryUsed();
            }
            long free = 0, total = 0;
            // com.sun.management.OperatingSystemMXBean(public interface)로 직접 캐스팅 — 비공개 impl 클래스에 리플렉션하면 IllegalAccess 로 실패한다.
            if (ManagementFactory.getOperatingSystemMXBean() instanceof com.sun.management.OperatingSystemMXBean sun) {
                free = sun.getFreeMemorySize();
                total = sun.getTotalMemorySize();
            }
            return new Snapshot(label, mem.getHeapMemoryUsage().getUsed(), mem.getNonHeapMemoryUsage().getUsed(),
                    meta, code, direct, free, total);
        }

        void print() {
            System.out.println("\n[② baseline — 힙/비힙/direct/free RAM] " + label);
            System.out.printf("  heap used         : %,d MB%n", heapUsed / (1024 * 1024));
            System.out.printf("  non-heap used     : %,d MB (metaspace %,d MB · code cache %,d MB)%n",
                    nonHeapUsed / (1024 * 1024), metaspace / (1024 * 1024), codeCache / (1024 * 1024));
            System.out.printf("  direct buffer     : %,d MB%n", directBuffer / (1024 * 1024));
            System.out.printf("  free / total RAM  : %,d MB / %,d MB%n", freeRam / (1024 * 1024), totalRam / (1024 * 1024));
        }
    }

    /** 사진 수 스윕: days=366 고정, 사진 수만 증가. 낮은 -Xmx 면 OOM 직전 행 = 안정 사진 수 상한. */
    private void runPhotoSweep(Pipeline pipe) {
        sweepHeader("사진수", "days=" + DAYS + " 고정, 사진 수만 증가");
        warmupOnce(pipe);
        for (String tok : SWEEP_PHOTOS.split(",")) {
            int n = Integer.parseInt(tok.trim());
            sweepRow(pipe, PdfSyntheticData.ofPhotoCount(END, DAYS, n), n + "장");
        }
        sweepFooter("사진 수");
    }

    /** 기간(일수) 스윕(★주 레버): 사진 100%(최악) 고정, 일수 증가 → 답변+리포트+사진 co-vary = 실제 period 상한. */
    private void runDaysSweep(Pipeline pipe) {
        sweepHeader("기간", "photoRatio=" + PHOTO_RATIO + "(사진100%=최악) 고정, 일수만 증가");
        warmupOnce(pipe);
        for (String tok : SWEEP_DAYS.split(",")) {
            int d = Integer.parseInt(tok.trim());
            sweepRow(pipe, PdfSyntheticData.of(END, d, PHOTO_RATIO), d + "일");
        }
        sweepFooter("기간");
    }

    private void sweepHeader(String varName, String fixed) {
        System.out.printf("%n[%s 스윕 — export 상한 사이징] %s · -Xmx=%,d MB · 배경점유=%d MB · 사진소스=%s%n",
                varName, fixed, Runtime.getRuntime().maxMemory() / (1024 * 1024), BACKGROUND_MB, PdfSyntheticData.lastPhotoSourceInfo());
        System.out.println("  변수 | 답변 | 사진 | ★sampler peak | live=resident(gc후·xhtml held) | wall | XHTML chars");
        System.out.println("  (낮은 -Xmx 면 OOM 직전 행 = 그 힙의 안정 상한. peak 는 렌더 중 최댓값·2g면 쓰레기 섞여 비단조)");
    }

    private void sweepFooter(String varName) {
        System.out.println("  → 이 -Xmx 에서 완주한 마지막 행 = 안정 " + varName + " 상한. prod 예산(512−앱baseline)에 맞춰 -PbenchXmx 낮춰 재실행.");
    }

    /** 동시 워크로드 모사: N MB 를 live 로 고정(제로페이지 회피 위해 채움). 렌더가 이 위에서 돌아 실제 공존 상황을 잰다. */
    private void allocateBackground() {
        if (BACKGROUND_MB <= 0) {
            return;
        }
        for (int i = 0; i < BACKGROUND_MB; i++) {
            byte[] block = new byte[1024 * 1024];
            java.util.Arrays.fill(block, (byte) ((i & 0x3F) + 1)); // 커밋 강제(0페이지 공유 회피)
            background.add(block);
        }
        settle();
        System.out.printf("%n  ★ 배경 점유(동시 워크로드 모사) = %d MB pinned · 렌더는 (힙 − 이 배경 − 하네스baseline) 안에 들어가야 함%n",
                BACKGROUND_MB);
    }

    /** 첫 행 JIT 콜드 완화용 소규모 워밍업 1회(측정 제외). */
    private void warmupOnce(Pipeline pipe) {
        assertThat(pipe.render(PdfExportType.REPORT_AND_ANSWER, PdfSyntheticData.of(END, Math.min(14, DAYS), 1.0)))
                .hasSizeGreaterThan(1000);
        settle();
    }

    /** 한 데이터셋을 렌더하며 sampler peak·resident(xhtml held)·wall 을 측정·출력. OOM 나면 이전 행까지만 남고 이 행에서 죽음(=상한 신호). */
    private void sweepRow(Pipeline pipe, PdfSyntheticData data, String label) {
        int answers = data.answers().size();
        long photos = data.answers().stream().filter(a -> a.imageKey() != null).count();

        settle();
        resetHeapPoolPeaks();
        HeapSampler sampler = HeapSampler.start(memoryBean);
        long t0 = System.nanoTime();
        PdfHtmlAssembler.AssembledDocument doc = pipe.assemble(PdfExportType.REPORT_AND_ANSWER, data);
        byte[] pdf = pipe.renderer.render(doc.xhtml(), doc.inlineAssets());
        long wall = System.nanoTime() - t0;
        try {
            sampler.stop();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertThat(pdf).hasSizeGreaterThan(1000);

        // live = xhtml·data·pdf 를 붙든 채 gc → 렌더 종료 resident. ★xhtml 을 null 하지 않는다.
        settle();
        long live = memoryBean.getHeapMemoryUsage().getUsed();
        int xhtmlLen = doc.xhtml().length(); // gc 이후 doc 사용 → live 에 XHTML+사진 애셋맵 포함 보장
        System.out.printf("  %6s | %4d | %4d | %,7d MB | %,7d MB | %,7d ms | %,d%n",
                label, answers, photos, sampler.maxUsed() / (1024 * 1024), live / (1024 * 1024), ms(wall), xhtmlLen);
    }

    private void measureConcurrent(Pipeline pipe, int n) throws Exception {
        // 각 워커가 독립 데이터로 동시에 렌더 → 2vCPU 에서 wall 이 단일 대비 얼마나 팽창하는지.
        ExecutorService pool = Executors.newFixedThreadPool(n);
        try {
            List<PdfSyntheticData> datasets = java.util.stream.IntStream.range(0, n)
                    .mapToObj(i -> PdfSyntheticData.of(END.minusDays(i), DAYS, PHOTO_RATIO))
                    .toList();
            long t0 = System.nanoTime();
            List<Future<byte[]>> futures = datasets.stream()
                    .map(d -> pool.submit(() -> pipe.render(PdfExportType.REPORT_AND_ANSWER, d)))
                    .toList();
            for (Future<byte[]> f : futures) {
                assertThat(f.get()).hasSizeGreaterThan(1000);
            }
            long wall = System.nanoTime() - t0;
            System.out.printf("  %d개 동시 렌더 전체 wall : %,d ms (동시성 %d, 가용 코어 %d)%n",
                    n, ms(wall), n, Runtime.getRuntime().availableProcessors());
            System.out.printf("  → 단일 wall 과 비교해 ~1배면 코어당 1개 병렬 OK, ~%d배면 사실상 직렬(경합)%n", n);
        } finally {
            pool.shutdownNow();
        }
    }

    private void printEnvironment() {
        Runtime rt = Runtime.getRuntime();
        System.out.printf("  JVM            : %s %s%n", System.getProperty("java.vm.name"), System.getProperty("java.version"));
        System.out.printf("  OS             : %s %s / %s%n", System.getProperty("os.name"),
                System.getProperty("os.version"), System.getProperty("os.arch"));
        System.out.printf("  availableProcessors : %d%n", rt.availableProcessors());
        System.out.printf("  -Xmx(maxMemory)     : %,d MB  ← peak 을 읽으려면 넉넉해야 함(2g 권장)%n", rt.maxMemory() / (1024 * 1024));
        System.out.printf("  규모(days=%d, photoRatio=%.2f, concurrent=%b)%n", DAYS, PHOTO_RATIO, RUN_CONCURRENT);
        // 사진 소스는 프리플라이트에서 출력(여기선 아직 resolveVariants 전이라 미초기화).
    }

    /**
     * 측정 전 사진 파이프라인 검증: 모든 원본 변이를 실제로 디코드+640재인코딩해 보고, 빈값/실패면 즉시 실패시킨다.
     * (resolver 가 per-photo 실패를 삼키므로, 이 관문이 없으면 "사진 0장 렌더"를 조용히 측정할 수 있다.)
     */
    private void preflightPhotos() {
        int[] sizes = PdfSyntheticData.variantPhotoByteSizes(); // 디코드 실패 시 예외 전파
        System.out.println("\n[사진 프리플라이트] 원본 1280×1280 → 640 q0.82 재인코딩(프로덕션 coverSquareJpegBytes 동일)");
        System.out.printf("  사진 소스     : %s%n", PdfSyntheticData.lastPhotoSourceInfo());
        assertThat(sizes).as("원본 변이가 없음 — photoDir/합성 폴백 확인").isNotEmpty();
        int min = Integer.MAX_VALUE, max = 0;
        long total = 0;
        for (int s : sizes) {
            assertThat(s).as("640 재인코딩 data URI 가 비었음/실패 — webp 디코드(twelvemonkeys) 확인").isGreaterThan(1024);
            min = Math.min(min, s);
            max = Math.max(max, s);
            total += s;
        }
        System.out.printf("  변이 %d개 · 장당 JPEG 바이트(=asset 맵 상주 크기) : min %,d KB · avg %,d KB · max %,d KB%n",
                sizes.length, min / 1024, (int) (total / sizes.length) / 1024, max / 1024);
        System.out.println("  → 실사진이면 이 값이 곧 실물 기준(366장이 이 크기로 asset 맵에 raw 바이트 상주). 편차 크면 worst 쪽으로 편향 검토.");
    }

    /* ── low-level ── */

    private void resetHeapPoolPeaks() {
        for (MemoryPoolMXBean p : ManagementFactory.getMemoryPoolMXBeans()) {
            if (p.getType() == MemoryType.HEAP) p.resetPeakUsage();
        }
    }

    private long heapPoolPeakBytes() {
        long sum = 0;
        for (MemoryPoolMXBean p : ManagementFactory.getMemoryPoolMXBeans()) {
            if (p.getType() == MemoryType.HEAP && p.getPeakUsage() != null) sum += p.getPeakUsage().getUsed();
        }
        return sum;
    }

    private static long poolUsed(String nameContains) {
        long sum = 0;
        for (MemoryPoolMXBean p : ManagementFactory.getMemoryPoolMXBeans()) {
            if (p.getName() != null && p.getName().contains(nameContains) && p.getUsage() != null) {
                sum += p.getUsage().getUsed();
            }
        }
        return sum;
    }

    private long processCpuTimeNanos() {
        // 직접 캐스팅(비공개 impl 리플렉션은 IllegalAccess). 미지원 플랫폼이면 -1 → 사용 측에서 무시.
        if (ManagementFactory.getOperatingSystemMXBean() instanceof com.sun.management.OperatingSystemMXBean sun) {
            return sun.getProcessCpuTime();
        }
        return 0L;
    }

    /** -Dpdf.bench.dumpPdf 지정 시 결과 PDF 를 파일로 써서 배너·divider·그림자·아이콘·레이더 회귀를 눈으로 확인하게 한다. */
    private static void dumpPdfIfRequested(byte[] pdf) throws IOException {
        if (DUMP_PDF == null || DUMP_PDF.isBlank()) {
            return;
        }
        Path out = Path.of(DUMP_PDF);
        if (out.getParent() != null) {
            Files.createDirectories(out.getParent());
        }
        Files.write(out, pdf);
        System.out.printf("%n  ★ PDF 덤프 → %s (%,d KB) — 열어서 배너 라운드코너·divider·그림자·아이콘·레이더 눈검토%n",
                out.toAbsolutePath(), pdf.length / 1024);
    }

    private static void settle() {
        for (int i = 0; i < 3; i++) {
            System.gc();
            try { Thread.sleep(120); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }
    }

    private static long ms(long nanos) { return nanos / 1_000_000; }

    private static void line() { System.out.println("────────────────────────────────────────────────────────────"); }

    private static void invoke(Object bean, String method) throws Exception {
        Method m = bean.getClass().getDeclaredMethod(method);
        m.setAccessible(true);
        m.invoke(bean);
    }
}
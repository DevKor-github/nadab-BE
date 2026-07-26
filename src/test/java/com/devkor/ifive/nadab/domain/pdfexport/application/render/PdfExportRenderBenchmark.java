package com.devkor.ifive.nadab.domain.pdfexport.application.render;

import com.devkor.ifive.nadab.domain.pdfexport.application.helper.PdfPhotoPrefetcher;
import com.devkor.ifive.nadab.domain.pdfexport.core.dto.PdfAnswerRowDto;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportType;
import com.devkor.ifive.nadab.domain.pdfexport.support.PdfSyntheticData;
import com.devkor.ifive.nadab.global.core.pdf.PdfAssetLoader;
import com.openhtmltopdf.extend.FSStream;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.lang.management.BufferPoolMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    /** 지정 시 이 디렉터리의 .ttf(Bold→700·그외 400)로 임베드 폰트 교체(GSUB 제거 A/B용). 미지정이면 번들 Pretendard. */
    private static final String FONT_DIR = System.getProperty("pdf.bench.fontDir");
    /** 단일 조합만 -Xmx 브라켓하려는 모드: baos-mem|baos-scratch|file-mem|file-scratch. 지정 시 메인 test 스킵 + 변형 test는 이 조합 1개만 렌더. */
    private static final String RENDER_MODE = System.getProperty("pdf.bench.renderMode");
    /** >0 이면 렌더 중 힙이 이 MB 를 처음 넘는 순간 live class histogram 을 1회 캡처(peak 분포 규명). 예: -Dpdf.bench.histoAtMB=600 */
    private static final int HISTO_AT_MB = Integer.getInteger("pdf.bench.histoAtMB", 0);
    /** 지정 시 histoAtMB 트리거 순간 live heap 을 이 경로에 .hprof 로 덤프(Eclipse MAT dominator 분석용). 예: -Dpdf.bench.heapDump=build/render-peak.hprof */
    private static final String HEAP_DUMP = System.getProperty("pdf.bench.heapDump");
    /** false 면 워밍업 렌더를 건너뛴다(콜드 렌더 = JIT 미완성 상태 측정). */
    private static final boolean WARMUP = Boolean.parseBoolean(System.getProperty("pdf.bench.warmup", "true"));
    /** histoAtMB 의 assemble 단계 판(사진 디코드 구간 분포 규명). 렌더 단계와 독립으로 켠다. */
    private static final int HISTO_ASM_AT_MB = Integer.getInteger("pdf.bench.histoAsmAtMB", 0);
    /** true 면 사진 1장 처리시간 단계분해 + ImageIO 스트림 캐시 확인만 실행(메인 측정 스킵). 사진 원본만 있으면 되고 수십 초면 끝난다. */
    private static final boolean PHOTO_PROBE = Boolean.getBoolean("pdf.bench.photoProbe");
    /** 단계분해 반복 횟수(원본 변이 1개당). 값이 작으면 JIT·GC 노이즈가 섞인다. */
    private static final int PHOTO_PROBE_ITERS = Integer.getInteger("pdf.bench.photoProbeIters", 20);
    /** 사진 출력 한 변 px — 프로덕션 PHOTO_PX 와 같은 값(단계분해가 실제와 같은 규모로 돌게). */
    private static final int PROBE_PHOTO_PX = 640;
    /** 병렬 확장성 측정에 쓸 사진 장수(P 스레드로 나눠 처리). 작으면 스레드 기동 비용이 섞인다. */
    private static final int PROBE_SCALE_PHOTOS = Integer.getInteger("pdf.bench.probePhotos", 60);
    /**
     * 사진 해석 방식 A/B: 0 = 현행(assemble 중 한 장씩 받아서 바로 디코드) · 1+ = 프리페치(그 수만큼의 다운로드 스레드).
     * 다운로드만 병렬이고 디코드는 항상 순차다 — 디코드 병렬은 CPU 작업이라 코어를 늘려도 GC 와 다투기만 한다(측정 완료).
     */
    private static final int PHOTO_PARALLEL = Integer.getInteger("pdf.bench.photoParallel", 0);
    /** 프리페치 창 = 동시에 들고 있을 원본 장수(추가 힙 = 창 × 원본크기). */
    private static final int PHOTO_WINDOW = Integer.getInteger("pdf.bench.photoWindow", 4);
    /**
     * 사진 1장 다운로드에 넣을 인위 지연 ms — 벤치는 사진이 로컬이라 S3 왕복이 0 이라서 그걸 모사한다.
     * dev EC2 실측 46ms. ★ 이건 S3 실측이 아니라 "겹침 구조가 작동하는가"의 증명용이다.
     */
    private static final int S3_DELAY_MS = Integer.getInteger("pdf.bench.s3DelayMs", 0);

    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    /** 배경 점유 블록(동시 워크로드 모사). 인스턴스 필드라 테스트 내내 live → 렌더 측정 동안 heap 을 실제로 차지한다. */
    private final List<byte[]> background = new ArrayList<>();
    /** asset: URI 별 요청 횟수(benchRender 경로) — openhtmltopdf 가 이미지를 몇 번 요청하나(lazy 서빙 가능성 판단). */
    private final Map<String, Integer> assetRequests = new ConcurrentHashMap<>();

    @Test
    void 일년치_단일렌더_실측_3종() throws Exception {
        if (RENDER_MODE != null && !RENDER_MODE.isBlank()) {
            System.out.println("[메인 측정] 스킵 — renderMode 브라켓 모드(변형 test가 단일 조합만 렌더)");
            return;
        }
        if (PHOTO_PROBE) {
            System.out.println("[메인 측정] 스킵 — photoProbe 모드(사진 단계분해만 실행)");
            return;
        }
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
        Path warmPdf = pipe.render(PdfExportType.REPORT_AND_ANSWER, warm);
        assertThat(fileSize(warmPdf)).isGreaterThan(1000);
        deleteQuietly(warmPdf);
        warm = null; // 워밍업 데이터가 1년치 측정 중 힙에 남지 않도록(합성 모드의 변이 PNG 오염 제거)
        settle();

        // ── ② baseline(렌더 전 안정 상태) ──
        Snapshot.capture("baseline(렌더 전, gc 후)").print();

        // ── ① peak heap + 단계별 시간(단일 1년치 렌더) ──
        System.out.println("\n[① 단일 1년치 렌더 — peak heap · 단계별 시간]");
        long cpuBefore = processCpuTimeNanos();

        // assemble 단계 peak 를 별도로 잰다 → floor 가 디코드(assemble)냐 레이아웃(렌더)이냐 판정용.
        resetHeapPoolPeaks();
        HeapSampler asmSampler = HeapSampler.start(memoryBean, (long) HISTO_ASM_AT_MB * 1024 * 1024);
        long t0 = System.nanoTime();
        PdfHtmlAssembler.AssembledDocument doc = pipe.assemble(PdfExportType.REPORT_AND_ANSWER, data);
        long t1 = System.nanoTime();
        asmSampler.stop();
        // ★ 이 줄이 낮은 -Xmx OOM 때 "assemble 완료" 마커 — 안 찍히면 assemble-bound, 찍히면 렌더-bound.
        System.out.printf("%n  [phase] assemble 완료 · assemble peak(sampler) = %,d MB · 진입 heap = %,d MB%n",
                asmSampler.maxUsed() / (1024 * 1024), memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024));
        printHistogramIfCaptured(asmSampler, "assemble", HISTO_ASM_AT_MB);

        // assemble churn 을 완전 수거 → 렌더 sampler 가 깨끗한 heap 에서 렌더 자체 성장만 잡게(렌더 floor 구성 규명·경계 오염 제거).
        settle();
        resetHeapPoolPeaks();
        HeapSampler sampler = HeapSampler.start(memoryBean, (long) HISTO_AT_MB * 1024 * 1024);
        long renderStart = System.nanoTime();
        Path pdf = renderMeasured(pipe, doc);
        long t2 = System.nanoTime();
        long cpuAfter = processCpuTimeNanos();

        sampler.stop();
        long heapPoolPeak = heapPoolPeakBytes();

        assertThat(pdfHeader(pdf)).startsWith("%PDF-");
        dumpPdfIfRequested(pdf);
        long pdfSize = fileSize(pdf);

        System.out.printf("  답변 %d · 주간 %d · 월간V2 %d · 사진 %d장%n",
                data.answers().size(), data.weeklies().size(), data.monthlyV2s().size(),
                data.answers().stream().filter(a -> a.imageKey() != null).count());
        System.out.printf("  assemble(사진 디코드+640재인코딩 포함) : %,d ms%n", ms(t1 - t0));
        System.out.printf("  render(openhtmltopdf → PDF)          : %,d ms%n", ms(t2 - renderStart));
        System.out.printf("  전체 렌더 wall                        : %,d ms%n", ms(t2 - t0));
        System.out.printf("  XHTML 길이                            : %,d chars (사진 asset: 서빙이라 XHTML 엔 base64 없음 — 텍스트/토큰만)%n", doc.xhtml().length());
        System.out.printf("  사진 asset 수(= 인라인 맵 크기)         : %,d장 (raw JPEG 바이트로 상주, base64/UTF-16 인플레이션 없음)%n", doc.inlineAssets().size());
        printShadowCacheStats(doc.xhtml());
        System.out.printf("  결과 PDF 크기                         : %,d bytes (%,d KB)%n", pdfSize, pdfSize / 1024);
        deleteQuietly(pdf); // PDF 는 파일에 있고 힙엔 없다(A) → 이후 resident 는 doc(xhtml+asset맵)만 반영
        System.out.printf("  peak heap(샘플러 max, ⚠미수거 쓰레기 포함): %,d bytes (%,d MB) — 힙 여유가 크면 G1 이 늦게 수거해 부풀음. 레버 비교·완주 판정에 쓰지 말 것(결정지표 = 완주 최소 -Xmx)%n",
                sampler.maxUsed(), sampler.maxUsed() / (1024 * 1024));
        System.out.printf("  peak heap(풀별 peak 합산, ⚠과다계상)    : %,d bytes (%,d MB) — Eden/Survivor/Old 의 서로 다른 시점 peak 합이라 -Xmx 초과 가능(참고만)%n",
                heapPoolPeak, heapPoolPeak / (1024 * 1024));
        System.out.printf("  ★ 임시파일 peak(java.io.tmpdir 증가분)   : %,d bytes (%,d MB) — A(PDF 출력)+scratch(PDFBox)가 힙 밖으로 옮긴 양. tmpdir 이 tmpfs 면 이만큼이 힙 대신 RAM%n",
                sampler.maxTmpBytes(), sampler.maxTmpBytes() / (1024 * 1024));

        // xhtml·data(asset맵) 를 여전히 붙든 채 gc → 렌더 종료 시점의 live-ish 유지량(PDF 는 파일이라 힙에 없음, peak 의 미수집 쓰레기 제외한 하한).
        settle();
        long settledUsed = memoryBean.getHeapMemoryUsage().getUsed();
        System.out.printf("  post-render 유지(gc 후 live-ish)         : %,d MB  ← 완주 필요 힙은 (이 값 ~ peak) 사이, 결정값은 -PbenchXmx 탐색%n",
                settledUsed / (1024 * 1024));

        printHistogramIfCaptured(sampler);

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

    /**
     * 사진 1장 처리시간(assemble 의 90%)이 어느 단계로 가는지 분해하고, ImageIO 가 이미 메모리에 있는 바이트를
     * 임시파일로 왕복시키는지 확인한다. -Dpdf.bench.photoProbe=true 로만 실행(사진 원본만 필요, 렌더 안 함).
     * 단계 = 스트림준비 / webp 디코드 / 스트림정리 / 크롭·리샘플 / JPEG 인코딩.
     */
    @Test
    void 사진_단계분해_및_ImageIO_스트림캐시_확인() throws Exception {
        if (!PHOTO_PROBE) {
            System.out.println("[사진 단계분해] 스킵(-Dpdf.bench.photoProbe=true 로 활성)");
            return;
        }
        byte[][] variants = PdfSyntheticData.photoSourceVariants();

        line();
        System.out.println("[사진 단계분해] 사진 1장 처리시간의 정체 + ImageIO 임시파일 왕복 확인");
        line();
        System.out.printf("  사진 소스 : %s%n", PdfSyntheticData.lastPhotoSourceInfo());
        System.out.printf("  원본 %d개 · 변이당 %d회 반복 · 출력 %d px%n", variants.length, PHOTO_PROBE_ITERS, PROBE_PHOTO_PX);

        printImageIoStreamEvidence(variants[0]);

        // 워밍업: JIT 이 안 데워진 첫 수 회는 배 이상 느려 단계 비율까지 왜곡한다(측정 제외).
        for (byte[] v : variants) {
            for (int i = 0; i < 5; i++) {
                PdfImage.coverSquareJpegBytes(v, PROBE_PHOTO_PX);
                photoStagesOnce(v, PROBE_PHOTO_PX, false, false);
            }
        }
        settle();

        System.out.println("\n  [단계분해] 값 = 사진 1장당 평균 ms");
        System.out.println("  구성                     | 스트림준비 | webp디코드 | 스트림정리 | 크롭·리샘플 | JPEG인코딩 |  합계");
        StageTimes base = probeStages(variants, "현행(ImageIO 기본)      ", false, false);
        probeStages(variants, "입력만 메모리스트림     ", true, false);
        probeStages(variants, "출력만 메모리스트림     ", false, true);
        StageTimes both = probeStages(variants, "입·출력 모두 메모리     ", true, true);

        // 복제 파이프라인이 프로덕션과 같은 일을 하는지 대조 — 어긋나면 위 단계 비율을 믿으면 안 된다.
        double prodMs = productionAverageMs(variants);
        double replicaMs = base.totalNanos() / 1e6;
        System.out.printf("%n  [대조군] 프로덕션 PdfImage.coverSquareJpegBytes 평균 = %.1f ms · 복제 현행 = %.1f ms (차이 %+.1f%%)%n",
                prodMs, replicaMs, (replicaMs - prodMs) / prodMs * 100);
        System.out.println("  → 차이가 ±5% 안이면 복제가 프로덕션과 같은 일을 한 것 = 단계 비율 신뢰 가능.");

        System.out.printf("%n  [디코드 출력 크기] %d×%d — 640 이면 리더가 서브샘플링을 적용한 것이라 그 다음 크롭·리샘플은 사실상 복사(줄일 여지 없음).%n",
                base.srcW(), base.srcH());
        System.out.printf("  [메모리스트림 효과] 현행 %.1f ms → 입·출력 모두 메모리 %.1f ms (%+.1f%%)%n",
                base.totalNanos() / 1e6, both.totalNanos() / 1e6,
                (both.totalNanos() - base.totalNanos()) / (double) base.totalNanos() * 100);
        System.out.println("  → 이득이 있으면 채택 형태는 ImageIO.setUseCache(false)(스레드별 전역)보다");
        System.out.println("     MemoryCacheImageInput/OutputStream 직접 생성이 안전(전역 상태 없음 · 병렬 디코드에도 그대로).");

        System.out.printf("%n  [규모 환산 — ⚠️참고만] 366장 = 현행 %.1f s / 메모리스트림 %.1f s · 20장 = %.1f s / %.1f s%n",
                base.totalNanos() * 366 / 1e9, both.totalNanos() * 366 / 1e9,
                base.totalNanos() * 20 / 1e9, both.totalNanos() * 20 / 1e9);
        System.out.println("  → 프로브 절대치는 실렌더보다 ~26% 부푼다(맵을 안 쌓아 GC 리듬이 다름). 절대 시간은 사진수 스윕 곡선을 쓸 것.");

        probeScaling(variants);
        line();
    }

    /**
     * 같은 장수를 P 스레드로 나눠 처리해 처리량이 P 에 비례하는지 본다.
     * 이 프로브는 결과 바이트를 버려서 asset 맵이 안 쌓인다 = 실렌더보다 GC 압력이 훨씬 낮은 문맥.
     * 여기서 P=2 가 확장되면 실렌더의 미확장 원인은 GC·힙 쪽이고, 여기서도 안 되면 디코드 작업 자체가 확장이 안 되는 것.
     * 모드 = 전체(디코드+크롭+인코딩) vs 디코드만 → 확장을 막는 게 디코더인지 그 뒤 단계인지 가른다.
     */
    private void probeScaling(byte[][] variants) throws Exception {
        System.out.printf("%n  [병렬 확장성] 사진 %d장을 P 스레드로 나눠 처리 — 처리량이 P 에 비례하는가 (가용 코어 %d)%n",
                PROBE_SCALE_PHOTOS, Runtime.getRuntime().availableProcessors());
        System.out.println("  모드     | P | wall | 처리량(장/s) | 배수 | CPU | CPU/장 | 코어");
        for (boolean decodeOnly : new boolean[]{false, true}) {
            double baseThroughput = 0;
            for (int parallelism = 1; parallelism <= 3; parallelism++) {
                settle();
                long cpuBefore = processCpuTimeNanos();
                long t0 = System.nanoTime();
                runScale(variants, PROBE_SCALE_PHOTOS, parallelism, decodeOnly);
                long wall = System.nanoTime() - t0;
                long cpu = processCpuTimeNanos() - cpuBefore;
                double throughput = PROBE_SCALE_PHOTOS / (wall / 1e9);
                if (parallelism == 1) {
                    baseThroughput = throughput;
                }
                System.out.printf("  %-8s | %d | %,6d ms | %10.1f | %.2f× | %5.1f s | %6.1f ms | %.2f%n",
                        decodeOnly ? "디코드만" : "전체", parallelism, ms(wall), throughput,
                        throughput / baseThroughput, cpu / 1e9,
                        cpu / 1e6 / PROBE_SCALE_PHOTOS, cpu / (double) wall);
            }
        }
        System.out.println("  → 배수가 P 에 가까우면 확장 O. 1.0 근처면 코어를 늘려도 처리량이 안 늘어난다는 뜻");
        System.out.println("     (그때 CPU/장 이 같이 오르면 = 늘어난 코어가 일 없이 태워진 것).");
    }

    /** count 장을 parallelism 스레드로 나눠 처리(1 이면 호출 스레드에서 순차). */
    private static void runScale(byte[][] variants, int count, int parallelism, boolean decodeOnly) throws Exception {
        if (parallelism <= 1) {
            for (int i = 0; i < count; i++) {
                scaleTask(variants[i % variants.length], decodeOnly);
            }
            return;
        }
        ExecutorService pool = Executors.newFixedThreadPool(parallelism);
        try {
            List<Future<?>> futures = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                byte[] source = variants[i % variants.length];
                futures.add(pool.submit(() -> scaleTask(source, decodeOnly)));
            }
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            pool.shutdownNow();
        }
    }

    /** 사진 1장 처리 — 전체(프로덕션 경로 그대로) 또는 디코드만. 결과는 검증만 하고 버린다(맵 미적재). */
    private static Object scaleTask(byte[] source, boolean decodeOnly) {
        try {
            if (decodeOnly) {
                BufferedImage decoded = decodeOnlyImage(source, PROBE_PHOTO_PX);
                assertThat(decoded.getWidth()).isGreaterThan(0);
            } else {
                assertThat(PdfImage.coverSquareJpegBytes(source, PROBE_PHOTO_PX).length).isGreaterThan(1024);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return null;
    }

    /** PdfImage.decodeSubsampled 복제 — 크롭·인코딩 없이 디코드 비용만 남긴다. */
    private static BufferedImage decodeOnlyImage(byte[] source, int size) throws IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(source))) {
            ImageReader reader = ImageIO.getImageReaders(iis).next();
            try {
                reader.setInput(iis, true, true);
                int shorter = Math.min(reader.getWidth(0), reader.getHeight(0));
                ImageReadParam param = reader.getDefaultReadParam();
                int subsample = Math.max(1, shorter / size);
                param.setSourceSubsampling(subsample, subsample, 0, 0);
                return reader.read(0, param);
            } finally {
                reader.dispose();
            }
        }
    }

    /** 원본별 640 재인코딩 총시간(프로덕션 경로 그대로) 평균 ms — 복제 단계분해의 대조군. */
    private static double productionAverageMs(byte[][] variants) {
        long sum = 0;
        int n = 0;
        for (byte[] v : variants) {
            for (int i = 0; i < PHOTO_PROBE_ITERS; i++) {
                long t0 = System.nanoTime();
                PdfImage.coverSquareJpegBytes(v, PROBE_PHOTO_PX);
                sum += System.nanoTime() - t0;
                n++;
            }
        }
        return sum / (double) n / 1e6;
    }

    /**
     * ImageIO 가 실제로 어떤 스트림 구현을 쓰는지 직접 출력한다(시간 추론이 아니라 클래스 이름으로 확정).
     * FileCache* 면 이미 힙에 있는 바이트를 임시파일에 썼다가 다시 읽는 것 — 서버 /tmp 는 tmpfs 라 RAM 왕복이 된다.
     */
    private static void printImageIoStreamEvidence(byte[] sample) throws IOException {
        System.out.println("\n  [ImageIO 스트림 캐시 — 직접 증거]");
        System.out.printf("    ImageIO.getUseCache()   : %b%n", ImageIO.getUseCache());
        System.out.printf("    ImageIO.getCacheDirectory(): %s (null 이면 java.io.tmpdir 사용)%n", ImageIO.getCacheDirectory());
        long before = tmpImageIoFileCount();
        try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(sample))) {
            System.out.printf("    createImageInputStream  → %s%n", in.getClass().getName());
            System.out.printf("    스트림 연 동안 tmpdir 의 imageio*.tmp : %d개 (열기 전 %d개)%n", tmpImageIoFileCount(), before);
        }
        try (ImageOutputStream out = ImageIO.createImageOutputStream(new ByteArrayOutputStream())) {
            System.out.printf("    createImageOutputStream → %s%n", out.getClass().getName());
        }
        System.out.println("    → FileCache* 면 임시파일 왕복이 실재. MemoryCache* 면 이 레버는 여기서 종료.");
    }

    private static long tmpImageIoFileCount() {
        File[] files = new File(System.getProperty("java.io.tmpdir")).listFiles();
        if (files == null) {
            return 0;
        }
        long n = 0;
        for (File f : files) {
            if (f.isFile() && f.getName().startsWith("imageio")) n++;
        }
        return n;
    }

    /** 한 구성으로 모든 변이를 반복 측정해 단계별 평균을 출력하고, 그 평균을 돌려준다. */
    private StageTimes probeStages(byte[][] variants, String label, boolean memoryIn, boolean memoryOut) throws IOException {
        long stream = 0, read = 0, close = 0, cover = 0, encode = 0;
        int n = 0, w = 0, h = 0;
        for (byte[] v : variants) {
            for (int i = 0; i < PHOTO_PROBE_ITERS; i++) {
                StageTimes t = photoStagesOnce(v, PROBE_PHOTO_PX, memoryIn, memoryOut);
                stream += t.streamNanos();
                read += t.readNanos();
                close += t.closeNanos();
                cover += t.coverNanos();
                encode += t.encodeNanos();
                w = t.srcW();
                h = t.srcH();
                n++;
            }
        }
        StageTimes avg = new StageTimes(stream / n, read / n, close / n, cover / n, encode / n, w, h);
        System.out.printf("  %s | %8.1f | %9.1f | %8.1f | %10.1f | %9.1f | %5.1f%n",
                label, avg.streamNanos() / 1e6, avg.readNanos() / 1e6, avg.closeNanos() / 1e6,
                avg.coverNanos() / 1e6, avg.encodeNanos() / 1e6, avg.totalNanos() / 1e6);
        return avg;
    }

    /**
     * PdfImage 파이프라인 복제 + 단계별 타이머(프로덕션 메서드가 private 이라 복제. 프로덕션과의 총시간 대조로 검증한다).
     * memoryIn/memoryOut = ImageIO 기본 스트림 대신 메모리캐시 스트림을 직접 생성(임시파일 왕복 제거).
     */
    private static StageTimes photoStagesOnce(byte[] source, int size, boolean memoryIn, boolean memoryOut) throws IOException {
        long t0 = System.nanoTime();
        ImageInputStream iis = memoryIn
                ? new MemoryCacheImageInputStream(new ByteArrayInputStream(source))
                : ImageIO.createImageInputStream(new ByteArrayInputStream(source));
        Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
        if (!readers.hasNext()) {
            throw new IOException("이미지 디코드 실패(지원하지 않는 포맷)");
        }
        ImageReader reader = readers.next();
        reader.setInput(iis, true, true);
        int shorter = Math.min(reader.getWidth(0), reader.getHeight(0));
        int subsample = Math.max(1, shorter / size);
        ImageReadParam param = reader.getDefaultReadParam();
        param.setSourceSubsampling(subsample, subsample, 0, 0);
        long t1 = System.nanoTime();

        BufferedImage src = reader.read(0, param);
        long t2 = System.nanoTime();

        reader.dispose();
        iis.close();
        long t3 = System.nanoTime();

        BufferedImage out = coverSquareReplica(src, size);
        long t4 = System.nanoTime();

        byte[] jpeg = encodeJpegReplica(out, memoryOut);
        long t5 = System.nanoTime();
        assertThat(jpeg.length).isGreaterThan(1024);

        return new StageTimes(t1 - t0, t2 - t1, t3 - t2, t4 - t3, t5 - t4, src.getWidth(), src.getHeight());
    }

    /** PdfImage.coverSquare 복제. */
    private static BufferedImage coverSquareReplica(BufferedImage src, int size) {
        int w = src.getWidth();
        int h = src.getHeight();
        int crop = Math.min(w, h);
        int sx = (w - crop) / 2;
        int sy = (h - crop) / 2;
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, size, size);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(src, 0, 0, size, size, sx, sy, sx + crop, sy + crop, null);
        } finally {
            g.dispose();
        }
        return out;
    }

    /** PdfImage.encodeJpeg 복제 + 출력 스트림 구현 토글. */
    private static byte[] encodeJpegReplica(BufferedImage img, boolean memoryOut) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.82f);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream ios = memoryOut ? new MemoryCacheImageOutputStream(baos) : ImageIO.createImageOutputStream(baos);
        try {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(img, null, null), param);
        } finally {
            ios.close();
            writer.dispose();
        }
        return baos.toByteArray();
    }

    /** 사진 1장 단계별 소요시간(ns)과 디코더가 내놓은 원본 크기. */
    private record StageTimes(long streamNanos, long readNanos, long closeNanos, long coverNanos, long encodeNanos,
                              int srcW, int srcH) {
        long totalNanos() {
            return streamNanos + readNanos + closeNanos + coverNanos + encodeNanos;
        }
    }

    /**
     * 카드 그림자 PNG 를 몇 종류나 굽는지(=캐시 히트율). 카드 높이가 내용맞춤이라 크기가 제각각이면
     * 크기별 캐시가 안 맞아 카드마다 박스블러를 새로 돌린다. 굽는 횟수 = 고유 크기 수.
     */
    private static void printShadowCacheStats(String xhtml) {
        Matcher m = Pattern.compile("asset:shadow-(\\d+)x(\\d+)").matcher(xhtml);
        Set<String> distinct = new HashSet<>();
        int total = 0;
        while (m.find()) {
            total++;
            distinct.add(m.group(1) + "x" + m.group(2));
        }
        if (total == 0) {
            return;
        }
        System.out.printf("  그림자 PNG                            : 참조 %,d회 · 고유 크기 %,d개(=굽는 횟수) · 캐시 히트율 %.1f%%%n",
                total, distinct.size(), (total - distinct.size()) * 100.0 / total);
    }

    /**
     * A(파일 스트리밍)·scratch(PDFBox 디스크 spill) 레버의 peak 델타를 4조합으로 격리 측정(2g 권장·단일렌더).
     * -Dpdf.bench.renderVariants=true 로만 실행(기본 pdfBench 는 스킵). 규모는 DAYS/PHOTO_RATIO(worst=366·1.0).
     * 조합 = 출력 sink(힙 BAOS vs 임시파일) × PDFBox 문서캐시(메모리 기본 vs 디스크 임시파일).
     */
    @Test
    void 렌더_출력_변형_실측_A_scratch() throws Exception {
        if (!Boolean.getBoolean("pdf.bench.renderVariants")) {
            System.out.println("[렌더 출력 변형 실측] 스킵(-Dpdf.bench.renderVariants=true 로 활성)");
            return;
        }
        Pipeline pipe = Pipeline.production();
        preflightPhotos();
        allocateBackground();
        warmupOnce(pipe);

        PdfSyntheticData data = PdfSyntheticData.of(END, DAYS, PHOTO_RATIO);
        PdfHtmlAssembler.AssembledDocument doc = pipe.assemble(PdfExportType.REPORT_AND_ANSWER, data);
        // 낮은 -Xmx 브라켓용 마커 — 이 줄이 안 찍히고 OOM 이면 assemble-bound, 찍힌 뒤 죽으면 render-bound.
        System.out.printf("%n  [phase] assemble 완료 · 진입 heap = %,d MB%n",
                memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024));
        List<PdfAssetLoader.FontFace> faces = benchFaces(pipe);

        System.out.printf("%n[렌더 출력 변형 실측 — A(파일)·scratch(디스크)] days=%d · photoRatio=%.2f · -Xmx=%,d MB · 배경%d MB · 폰트=%s%n",
                DAYS, PHOTO_RATIO, Runtime.getRuntime().maxMemory() / (1024 * 1024), BACKGROUND_MB,
                (FONT_DIR == null || FONT_DIR.isBlank()) ? "번들" : ("fontDir=" + FONT_DIR));
        System.out.println("  조합            | sampler peak(⚠쓰레기포함) | resident(gc후) | wall | 임시파일 peak");

        if (RENDER_MODE != null && !RENDER_MODE.isBlank()) {
            // 단일 조합만 렌더 → -Xmx 브라켓용(OOM=이진, 노이즈 없음). 완주하면 PASS, 못 들어가면 이 렌더에서 OOM.
            boolean toFile = RENDER_MODE.startsWith("file");
            boolean scratch = RENDER_MODE.endsWith("scratch");
            System.out.printf("  [브라켓 단일] mode=%s (toFile=%b, scratch=%b) · -Xmx=%,d MB%n",
                    RENDER_MODE, toFile, scratch, Runtime.getRuntime().maxMemory() / (1024 * 1024));
            renderVariantRow(pipe, doc, faces, RENDER_MODE, toFile, scratch);
            System.out.println("  → 이 -Xmx 에서 완주하면 그 조합의 peak ≤ -Xmx. OOM 이면 초과. 조합별로 -PbenchXmx 낮춰 floor 비교.");
            return;
        }

        renderVariantRow(pipe, doc, faces, "BAOS·mem(현행)", false, false);
        renderVariantRow(pipe, doc, faces, "BAOS·scratch ", false, true);
        renderVariantRow(pipe, doc, faces, "file·mem     ", true, false);
        renderVariantRow(pipe, doc, faces, "file·scratch ", true, true);

        System.out.println("  → BAOS→file 델타 = A(꼬리 스파이크+held), mem→scratch 델타 = PDFBox 디스크 spill. 반드시 2g(언클램프)에서 읽을 것.");
    }

    private void renderVariantRow(Pipeline pipe, PdfHtmlAssembler.AssembledDocument doc,
                                  List<PdfAssetLoader.FontFace> faces, String label, boolean toFile, boolean scratch) throws Exception {
        settle();
        assetRequests.clear();
        resetHeapPoolPeaks();
        HeapSampler sampler = HeapSampler.start(memoryBean);
        Path tmp = toFile ? Files.createTempFile("pdfbench-", ".pdf") : null;
        long t0 = System.nanoTime();
        try (OutputStream sink = toFile ? Files.newOutputStream(tmp) : new ByteArrayOutputStream()) {
            benchRender(doc.xhtml(), doc.inlineAssets(), pipe, faces, sink, scratch);
        } finally {
            if (tmp != null) Files.deleteIfExists(tmp);
        }
        long wall = System.nanoTime() - t0;
        try { sampler.stop(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        settle();
        long resident = memoryBean.getHeapMemoryUsage().getUsed();
        System.out.printf("  %s | %,7d MB | %,7d MB | %,7d ms | 임시파일 %,d MB%n",
                label, sampler.maxUsed() / (1024 * 1024), resident / (1024 * 1024), ms(wall),
                sampler.maxTmpBytes() / (1024 * 1024));
        printAssetRequestSummary();
    }

    /** 이번 렌더에서 사진 asset(asset:photo-N)이 몇 번 요청됐나 — 1회면 lazy 서빙이 캐시 없이 깔끔, 2회+면 재리샘플/캐시 필요. */
    private void printAssetRequestSummary() {
        Map<Integer, Integer> dist = new TreeMap<>();   // 요청횟수 → 그런 사진 장수
        long total = 0;
        int distinct = 0;
        for (Map.Entry<String, Integer> e : assetRequests.entrySet()) {
            if (e.getKey().contains("photo-")) {
                distinct++;
                total += e.getValue();
                dist.merge(e.getValue(), 1, Integer::sum);
            }
        }
        double avg = distinct == 0 ? 0 : (double) total / distinct;
        System.out.printf("    └ 사진 asset 요청: 고유 %d장 · 총 %d회 · 평균 %.2f회/장 · 분포(요청수:장수)=%s%n",
                distinct, total, avg, dist);
    }

    /** 측정 대상 렌더: fontDir 지정 시 대체 폰트로 benchRender, 아니면 프로덕션 PdfRenderer 그대로(기본 동작 불변). 결과는 임시파일 Path. */
    private Path renderMeasured(Pipeline pipe, PdfHtmlAssembler.AssembledDocument doc) throws IOException {
        if (FONT_DIR == null || FONT_DIR.isBlank()) {
            return pipe.renderer.render(doc.xhtml(), doc.inlineAssets());
        }
        // fontDir A/B: 대체 폰트로 benchRender, 결과를 임시파일로(힙에 PDF 안 올림 — 측정 오염 방지).
        Path pdfFile = Files.createTempFile("pdfbench-font-", ".pdf");
        try (OutputStream os = Files.newOutputStream(pdfFile)) {
            benchRender(doc.xhtml(), doc.inlineAssets(), pipe, benchFaces(pipe), os, false);
        }
        return pdfFile;
    }

    /** fontDir 의 .ttf(파일명에 bold → 700, 그외 400)로 폰트 페이스 구성. 미지정이면 번들. */
    private List<PdfAssetLoader.FontFace> benchFaces(Pipeline pipe) throws IOException {
        if (FONT_DIR == null || FONT_DIR.isBlank()) {
            return pipe.assets.pretendardFaces();
        }
        List<PdfAssetLoader.FontFace> faces = new ArrayList<>();
        try (var paths = Files.list(Path.of(FONT_DIR))) {
            for (Path p : paths.filter(x -> x.toString().toLowerCase().endsWith(".ttf")).toList()) {
                int weight = p.getFileName().toString().toLowerCase().contains("bold") ? 700 : 400;
                faces.add(new PdfAssetLoader.FontFace(weight, Files.readAllBytes(p)));
            }
        }
        if (faces.isEmpty()) {
            throw new IllegalStateException("fontDir 에 .ttf 가 없음: " + FONT_DIR);
        }
        return faces;
    }

    /** PdfRenderer.render 를 복제하되 폰트·출력 sink·PDFBox 스크래치를 토글(측정 전용). 프로덕션 PdfRenderer 는 무변경. */
    private void benchRender(String xhtml, Map<String, byte[]> inlineAssets, Pipeline pipe,
                             List<PdfAssetLoader.FontFace> faces, OutputStream sink, boolean scratch) throws IOException {
        PdfRendererBuilder builder = new PdfRendererBuilder();
        for (PdfAssetLoader.FontFace face : faces) {
            byte[] fontData = face.data();
            builder.useFont(() -> new ByteArrayInputStream(fontData),
                    PdfAssetLoader.FONT_FAMILY, face.weight(), BaseRendererBuilder.FontStyle.NORMAL, true);
        }
        builder.useProtocolsStreamImplementation(uri -> benchOpenAsset(uri, inlineAssets, pipe), "asset");
        builder.withHtmlContent(xhtml, "");
        builder.toStream(sink);
        if (scratch) {
            // PDFBox 문서모델을 힙 대신 디스크 임시파일에 spill. usePDDocument 사용 시 close 는 우리 책임.
            try (PDDocument pdDoc = new PDDocument(IOUtils.createTempFileOnlyStreamCache())) {
                builder.usePDDocument(pdDoc);
                builder.run();
            }
        } else {
            builder.run();
        }
    }

    private FSStream benchOpenAsset(String uri, Map<String, byte[]> inlineAssets, Pipeline pipe) {
        assetRequests.merge(uri, 1, Integer::sum);   // openhtmltopdf 가 이 URI 를 몇 번 요청하나 집계
        String key = uri.substring(uri.indexOf(':') + 1);
        byte[] resolved;
        if (key.startsWith("photo-")) {
            resolved = inlineAssets.get(key);
        } else if (key.startsWith("shadow-")) {
            String[] wh = key.substring("shadow-".length()).split("x");
            resolved = pipe.shadow.bytes(Integer.parseInt(wh[0]), Integer.parseInt(wh[1]));
        } else {
            resolved = pipe.assets.assetBytes(key).orElseThrow();
        }
        byte[] bytes = resolved;
        return new FSStream() {
            @Override public InputStream getStream() { return new ByteArrayInputStream(bytes); }
            @Override public Reader getReader() { return new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8); }
        };
    }

    /* ────────────────────────── 렌더 파이프라인(협력자 실객체) ────────────────────────── */

    private static final class Pipeline {
        final PdfHtmlAssembler assembler;
        final PdfRenderer renderer;
        final PdfAssetLoader assets;
        final PdfShadowRenderer shadow;

        private Pipeline(PdfHtmlAssembler assembler, PdfRenderer renderer,
                         PdfAssetLoader assets, PdfShadowRenderer shadow) {
            this.assembler = assembler;
            this.renderer = renderer;
            this.assets = assets;
            this.shadow = shadow;
        }

        static Pipeline production() throws Exception {
            PdfAssetLoader assets = new PdfAssetLoader();
            invoke(assets, "load");
            PdfShadowRenderer shadow = new PdfShadowRenderer();
            PdfHtmlAssembler assembler = new PdfHtmlAssembler(assets, new EmotionRadarChartRenderer(), shadow);
            invoke(assembler, "loadCss");
            return new Pipeline(assembler, new PdfRenderer(assets, shadow), assets, shadow);
        }

        PdfHtmlAssembler.AssembledDocument assemble(PdfExportType type, PdfSyntheticData d) {
            Function<String, Optional<byte[]>> resolver;
            if (PHOTO_PARALLEL >= 1) {
                // 프리페치 경로(프로덕션과 같은 형태): 다운로드를 몇 장 앞서 돌리고 디코드는 순차.
                List<String> keys = d.answers().stream()
                        .map(PdfAnswerRowDto::imageKey)
                        .filter(Objects::nonNull)
                        .toList();
                Map<String, byte[]> photos = PdfPhotoPrefetcher.prefetch(keys,
                        key -> { simulateS3(); return d.photoSource(key); },
                        source -> PdfImage.coverSquareJpegBytes(source, PROBE_PHOTO_PX),
                        (key, e) -> { },
                        PHOTO_PARALLEL, PHOTO_WINDOW);
                resolver = key -> Optional.ofNullable(photos.get(key));
            } else {
                // 현행 경로: 어셈블러가 한 장씩 요청하고 그 자리에서 받아 디코드 → 대기와 디코드가 직렬로 더해진다.
                resolver = key -> {
                    simulateS3();
                    return d.photoResolver().apply(key);
                };
            }
            return assembler.assemble(type, d.answers(), d.weeklies(), d.monthlies(), d.monthlyV2s(), resolver);
        }

        Path render(PdfExportType type, PdfSyntheticData d) {
            PdfHtmlAssembler.AssembledDocument doc = assemble(type, d);
            return renderer.render(doc.xhtml(), doc.inlineAssets());
        }
    }

    /* PDF 는 임시파일로 나오므로(A: 힙에 안 올림) 크기·헤더는 파일에서 확인하고 측정 후 삭제한다. */

    private static long fileSize(Path p) {
        try {
            return Files.size(p);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String pdfHeader(Path p) {
        try (InputStream in = Files.newInputStream(p)) {
            byte[] b = new byte[5];
            int n = in.readNBytes(b, 0, 5);
            return new String(b, 0, n, StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void deleteQuietly(Path p) {
        try {
            Files.deleteIfExists(p);
        } catch (IOException ignored) {
            // 벤치 임시파일 — 삭제 실패해도 무시
        }
    }

    /* ────────────────────────── 측정 유틸 ────────────────────────── */

    /** 백그라운드 폴링으로 힙 used max 를 추적(풀 peak 과 교차검증). 옵션: 고수위서 live class histogram 1회 캡처. */
    private static final class HeapSampler {
        private final Thread thread;
        private final AtomicLong max = new AtomicLong(0);
        /** java.io.tmpdir 사용량의 최대 증가분(=이 렌더가 임시파일로 쓴 바이트). A·scratch 가 힙 밖으로 옮긴 양. */
        private final AtomicLong maxTmp = new AtomicLong(0);
        private final long tmpBaseline = tmpDirBytes();
        private volatile boolean running = true;
        private volatile String histogram;   // histoThreshold 넘는 순간 1회 캡처(peak 분포)
        private volatile long histogramAtUsed;

        private HeapSampler(MemoryMXBean bean, long histoThresholdBytes) {
            this.thread = new Thread(() -> {
                long tick = 0;
                while (running) {
                    long used = bean.getHeapMemoryUsage().getUsed();
                    max.accumulateAndGet(used, Math::max);
                    // 디렉터리 스캔은 비싸 힙 폴링보다 드물게(≈50ms) — baseline 대비 증가분만 센다(선재 파일 제외).
                    if ((++tick % 100) == 0) {
                        maxTmp.accumulateAndGet(tmpDirBytes() - tmpBaseline, Math::max);
                    }
                    if (histoThresholdBytes > 0 && histogram == null && used >= histoThresholdBytes) {
                        histogramAtUsed = used;
                        histogram = gcClassHistogram();  // full GC → live 히스토그램(1회, 이후 재캡처 안 함)
                        if (HEAP_DUMP != null && !HEAP_DUMP.isBlank()) {
                            dumpHeapLive(HEAP_DUMP);      // 같은 순간 live heap 을 .hprof 로(MAT dominator 분석용)
                        }
                    }
                    try { Thread.sleep(0, 500_000); } catch (InterruptedException e) { return; }
                }
            }, "heap-sampler");
            this.thread.setDaemon(true);
        }

        static HeapSampler start(MemoryMXBean bean) { return start(bean, 0); }

        static HeapSampler start(MemoryMXBean bean, long histoThresholdBytes) {
            HeapSampler s = new HeapSampler(bean, histoThresholdBytes);
            s.thread.start();
            return s;
        }

        void stop() throws InterruptedException {
            running = false;
            thread.join(2000);   // 히스토그램 캡처(full GC) 여유
        }

        long maxUsed() { return max.get(); }
        long maxTmpBytes() { return Math.max(0, maxTmp.get()); }
        String histogram() { return histogram; }
        long histogramAtUsed() { return histogramAtUsed; }
    }

    private static void printHistogramIfCaptured(HeapSampler sampler) {
        printHistogramIfCaptured(sampler, "render", HISTO_AT_MB);
    }

    private static void printHistogramIfCaptured(HeapSampler sampler, String phase, int thresholdMB) {
        String h = sampler.histogram();
        if (h == null) {
            if (thresholdMB > 0) {
                System.out.printf("  [histogram:%s] 힙이 %d MB 를 안 넘어 미캡처 — 임계를 더 낮출 것%n", phase, thresholdMB);
            }
            return;
        }
        System.out.printf("%n  ★ %s 단계 분포 (live class histogram @ 힙 %,d MB · full GC 후 live 바이트 · 상위 30) — GC.class_histogram 동일%n",
                phase, sampler.histogramAtUsed() / (1024 * 1024));
        h.lines().limit(33).forEach(l -> System.out.println("    " + l));
    }

    /** 힙이 고수위일 때 live 객체를 클래스별 바이트로(full GC 후). DiagnosticCommand MBean = jcmd GC.class_histogram 과 동일. */
    private static String gcClassHistogram() {
        try {
            javax.management.MBeanServer s = ManagementFactory.getPlatformMBeanServer();
            javax.management.ObjectName n = new javax.management.ObjectName("com.sun.management:type=DiagnosticCommand");
            return (String) s.invoke(n, "gcClassHistogram",
                    new Object[]{ new String[0] },
                    new String[]{ String[].class.getName() });
        } catch (Exception e) {
            return "class histogram 캡처 실패: " + e;
        }
    }

    /** live heap 을 .hprof 로 덤프(full GC 후 live only). Eclipse MAT 로 dominator tree = byte[] 누가 retain 하나 분석. */
    private static void dumpHeapLive(String path) {
        try {
            Files.deleteIfExists(Path.of(path));  // dumpHeap 은 기존 파일 있으면 실패
            com.sun.management.HotSpotDiagnosticMXBean bean =
                    ManagementFactory.getPlatformMXBean(com.sun.management.HotSpotDiagnosticMXBean.class);
            bean.dumpHeap(path, true);            // true = live objects only(덤프 전 full GC)
            System.out.printf("%n  ★ heap dump → %s (live) — Eclipse MAT 로 dominator tree 분석%n",
                    Path.of(path).toAbsolutePath());
        } catch (Exception e) {
            System.out.println("  heap dump 실패: " + e);
        }
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
        System.out.printf("  ★ 사진 해석 = %s%n", photoParallelLabel());
        System.out.println("  변수 | 답변 | 사진 | ⚠sampler peak | live=resident(gc후·xhtml held) | assemble | render | wall | CPU | 코어 | GC(회/STW) | XHTML chars");
        System.out.println("  (낮은 -Xmx 면 OOM 직전 행 = 그 힙의 안정 상한. peak 는 미수거 쓰레기 포함이라 판정에 쓰지 말 것)");
        System.out.println("  (★ assemble = 사진 디코드 구간 = 병렬화 대상 · render = 병렬화 무관 → A/B 시 render 가 같은 런 안 대조군)");
    }

    private void sweepFooter(String varName) {
        System.out.println("  → 이 -Xmx 에서 완주한 마지막 행 = 안정 " + varName + " 상한. prod 예산(512−앱baseline)에 맞춰 -PbenchXmx 낮춰 재실행.");
    }

    /** 동시 워크로드 모사: N MB 를 live 로 고정(제로페이지 회피 위해 채움). 렌더가 이 위에서 돌아 실제 공존 상황을 잰다. */
    private void allocateBackground() {
        if (BACKGROUND_MB <= 0) {
            return;
        }
        // 블록 256KB: G1 humongous 임계(리전 50%=512KB @ 1MB 리전) 미만이라 정상 패킹.
        // 1MB 블록은 1MB 리전을 넘겨 humongous(2리전)로 잡혀 실제 점유가 2배가 된다.
        int blockBytes = 256 * 1024;
        int blocks = BACKGROUND_MB * (1024 * 1024 / blockBytes);
        for (int i = 0; i < blocks; i++) {
            byte[] block = new byte[blockBytes];
            java.util.Arrays.fill(block, (byte) ((i & 0x3F) + 1)); // 커밋 강제(0페이지 공유 회피)
            background.add(block);
        }
        settle();
        System.out.printf("%n  ★ 배경 점유(동시 워크로드 모사) = %d MB pinned · 렌더는 (힙 − 이 배경 − 하네스baseline) 안에 들어가야 함%n",
                BACKGROUND_MB);
    }

    /** 첫 행 JIT 콜드 완화용 소규모 워밍업 1회(측정 제외). -Dpdf.bench.warmup=false 면 생략 = 콜드 렌더 측정. */
    private void warmupOnce(Pipeline pipe) {
        if (!WARMUP) {
            System.out.println("  [워밍업 생략] 콜드 렌더 측정(-Dpdf.bench.warmup=false)");
            settle();
            return;
        }
        Path warmPdf = pipe.render(PdfExportType.REPORT_AND_ANSWER, PdfSyntheticData.of(END, Math.min(14, DAYS), 1.0));
        assertThat(fileSize(warmPdf)).isGreaterThan(1000);
        deleteQuietly(warmPdf);
        settle();
    }

    /** 한 데이터셋을 렌더하며 sampler peak·resident(xhtml held)·wall 을 측정·출력. OOM 나면 이전 행까지만 남고 이 행에서 죽음(=상한 신호). */
    private void sweepRow(Pipeline pipe, PdfSyntheticData data, String label) {
        int answers = data.answers().size();
        long photos = data.answers().stream().filter(a -> a.imageKey() != null).count();

        settle();
        resetHeapPoolPeaks();
        HeapSampler sampler = HeapSampler.start(memoryBean);
        long cpuBefore = processCpuTimeNanos();
        long gcCountBefore = gcCollectionCount();
        long gcTimeBefore = gcCollectionTimeMs();
        long t0 = System.nanoTime();
        PdfHtmlAssembler.AssembledDocument doc = pipe.assemble(PdfExportType.REPORT_AND_ANSWER, data);
        long t1 = System.nanoTime();
        Path pdf = pipe.renderer.render(doc.xhtml(), doc.inlineAssets());
        long t2 = System.nanoTime();
        long wall = t2 - t0;
        // CPU-초 = 버스터블 크레딧이 보는 값. 병렬화는 wall 만 줄이고 이건 그대로거나 늘어난다 → 반드시 같이 본다.
        long cpuNanos = processCpuTimeNanos() - cpuBefore;
        // GC 횟수·STW = "늘어난 CPU 가 GC 로 갔나" 판정용. GC 로그 파싱 없이 같은 런 안에서 델타로 잡는다.
        long gcCount = gcCollectionCount() - gcCountBefore;
        long gcTime = gcCollectionTimeMs() - gcTimeBefore;
        try {
            sampler.stop();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertThat(fileSize(pdf)).isGreaterThan(1000);
        deleteQuietly(pdf); // PDF 는 파일(A) → resident 는 doc(xhtml+asset맵)만

        // live = xhtml·data(asset맵) 를 붙든 채 gc → 렌더 종료 resident. ★xhtml 을 null 하지 않는다.
        settle();
        long live = memoryBean.getHeapMemoryUsage().getUsed();
        int xhtmlLen = doc.xhtml().length(); // gc 이후 doc 사용 → live 에 XHTML+사진 애셋맵 포함 보장
        System.out.printf("  %6s | %4d | %4d | %,7d MB | %,7d MB | %,8d ms | %,6d ms | %,7d ms | %6.1f s | %.2f | %4d회/%,6d ms | %,d%n",
                label, answers, photos, sampler.maxUsed() / (1024 * 1024), live / (1024 * 1024),
                ms(t1 - t0), ms(t2 - t1), ms(wall),
                cpuNanos / 1e9, cpuNanos / (double) wall, gcCount, gcTime, xhtmlLen);
    }

    private void measureConcurrent(Pipeline pipe, int n) throws Exception {
        // 각 워커가 독립 데이터로 동시에 렌더 → 2vCPU 에서 wall 이 단일 대비 얼마나 팽창하는지.
        ExecutorService pool = Executors.newFixedThreadPool(n);
        try {
            List<PdfSyntheticData> datasets = java.util.stream.IntStream.range(0, n)
                    .mapToObj(i -> PdfSyntheticData.of(END.minusDays(i), DAYS, PHOTO_RATIO))
                    .toList();
            long t0 = System.nanoTime();
            List<Future<Path>> futures = datasets.stream()
                    .map(d -> pool.submit(() -> pipe.render(PdfExportType.REPORT_AND_ANSWER, d)))
                    .toList();
            for (Future<Path> f : futures) {
                Path p = f.get();
                assertThat(fileSize(p)).isGreaterThan(1000);
                deleteQuietly(p);
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
        System.out.printf("  임베드 폰트         : %s%n", (FONT_DIR == null || FONT_DIR.isBlank()) ? "번들 Pretendard" : ("대체 fontDir=" + FONT_DIR));
        System.out.printf("  사진 해석           : %s%n", photoParallelLabel());
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

    /** 지금까지의 GC 총 횟수(모든 수집기 합). 델타로 쓰면 그 구간의 GC 횟수. */
    private static long gcCollectionCount() {
        long sum = 0;
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = gc.getCollectionCount();
            if (count > 0) sum += count;
        }
        return sum;
    }

    /** 지금까지의 GC 총 STW 시간 ms(모든 수집기 합). GC 로그 파싱 없이 구간 델타로 STW 를 잡는다. */
    private static long gcCollectionTimeMs() {
        long sum = 0;
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            long time = gc.getCollectionTime();
            if (time > 0) sum += time;
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
    private static void dumpPdfIfRequested(Path pdf) throws IOException {
        if (DUMP_PDF == null || DUMP_PDF.isBlank()) {
            return;
        }
        Path out = Path.of(DUMP_PDF);
        if (out.getParent() != null) {
            Files.createDirectories(out.getParent());
        }
        Files.copy(pdf, out, StandardCopyOption.REPLACE_EXISTING);
        System.out.printf("%n  ★ PDF 덤프 → %s (%,d KB) — 열어서 배너 라운드코너·divider·그림자·아이콘·레이더 눈검토%n",
                out.toAbsolutePath(), fileSize(out) / 1024);
    }

    /** java.io.tmpdir 바로 아래 파일들의 총 바이트. 접근 실패는 0 으로 무시(측정 보조값). */
    private static long tmpDirBytes() {
        File[] files = new File(System.getProperty("java.io.tmpdir")).listFiles();
        if (files == null) {
            return 0;
        }
        long sum = 0;
        for (File f : files) {
            if (f.isFile()) {
                sum += f.length();
            }
        }
        return sum;
    }

    private static void settle() {
        for (int i = 0; i < 3; i++) {
            System.gc();
            try { Thread.sleep(120); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }
    }

    private static long ms(long nanos) { return nanos / 1_000_000; }

    /** S3 왕복 모사 — 벤치는 사진이 로컬이라 왕복이 0 이다. 겹침 구조가 실제로 대기를 가리는지 보려면 이게 필요하다. */
    private static void simulateS3() {
        if (S3_DELAY_MS <= 0) {
            return;
        }
        try {
            Thread.sleep(S3_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("S3 모사 지연 중단", e);
        }
    }

    /** 이번 런이 A/B 어느 팔인지 — 출력만 보고도 조건을 알 수 있게 매 표에 찍는다. */
    private static String photoParallelLabel() {
        String delay = S3_DELAY_MS > 0 ? (" · S3 모사 지연 " + S3_DELAY_MS + "ms/장") : " · S3 지연 없음";
        if (PHOTO_PARALLEL <= 0) {
            return "인라인(현행 · 받고→풀고 직렬)" + delay;
        }
        return "프리페치 창 " + PHOTO_WINDOW + " · 다운로드 스레드 " + PHOTO_PARALLEL + "(디코드는 순차)" + delay;
    }

    private static void line() { System.out.println("────────────────────────────────────────────────────────────"); }

    private static void invoke(Object bean, String method) throws Exception {
        Method m = bean.getClass().getDeclaredMethod(method);
        m.setAccessible(true);
        m.invoke(bean);
    }
}
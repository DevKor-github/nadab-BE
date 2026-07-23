package com.devkor.ifive.nadab.domain.pdfexport.support;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionCode;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlyEmotionComparisonContent;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReport;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportComparisonType;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportImageStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2Content;
import com.devkor.ifive.nadab.domain.pdfexport.application.render.PdfImage;
import com.devkor.ifive.nadab.domain.pdfexport.core.dto.PdfAnswerRowDto;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeTextContent;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReport;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReportStatus;
import com.devkor.ifive.nadab.global.shared.reportcontent.ReportContent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

/**
 * 렌더 실측(Phase 1)용 합성 데이터 빌더 — DB/Spring 0. 규모 조절(N일·사진비율·주간/월간V2).
 * jsonb content 는 ObjectMapper 역직렬화, V2 파생 필드(emotionStats/comparison/emotionSummary)는 세터가 없어 리플렉션 주입.
 * 사진 리졸버는 프로덕션과 동일한 PdfImage.coverSquareJpegBytes 를 태운다(디코드·리샘플·인코딩까지 실측 포함).
 */
public final class PdfSyntheticData {

    /** 답변 사진 임베드 한 변 px(프로덕션 리스너 PHOTO_PX 와 동일). */
    public static final int PHOTO_PX = 640;

    /** 합성 원본 한 변 px — 앱 answer webp(1280 정사각) 미러. */
    public static final int SOURCE_SIDE = 1280;

    /** 합성 원본 변이 수 — 답변마다 다른 원본 흉내(생성 비용은 측정 밖에서 1회). */
    private static final int SOURCE_VARIANTS = 12;

    /** 실제 answer webp 를 담은 디렉터리(선택). 지정 시 그 안의 *.webp 를 원본으로 사용 → 640 재인코딩 크기가 실물로 정확. 미지정/없음이면 합성 폴백. */
    public static final String PHOTO_DIR_PROP = "pdf.bench.photoDir";

    /** 마지막으로 결정된 사진 소스(실사진 N장 / 합성)를 하네스가 출력하도록. */
    private static volatile String lastPhotoSourceInfo = "(미초기화)";

    public static String lastPhotoSourceInfo() { return lastPhotoSourceInfo; }

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final List<PdfAnswerRowDto> answers;
    private final List<WeeklyReport> weeklies;
    private final List<MonthlyReport> monthlies;
    private final List<MonthlyReportV2> monthlyV2s;
    private final Function<String, Optional<byte[]>> photoResolver;

    private PdfSyntheticData(List<PdfAnswerRowDto> answers, List<WeeklyReport> weeklies,
                             List<MonthlyReport> monthlies, List<MonthlyReportV2> monthlyV2s,
                             Function<String, Optional<byte[]>> photoResolver) {
        this.answers = answers;
        this.weeklies = weeklies;
        this.monthlies = monthlies;
        this.monthlyV2s = monthlyV2s;
        this.photoResolver = photoResolver;
    }

    public List<PdfAnswerRowDto> answers() { return answers; }
    public List<WeeklyReport> weeklies() { return weeklies; }
    public List<MonthlyReport> monthlies() { return monthlies; }
    public List<MonthlyReportV2> monthlyV2s() { return monthlyV2s; }
    public Function<String, Optional<byte[]>> photoResolver() { return photoResolver; }

    /**
     * 1년치 최악 케이스: endDate 기준 days 일 전부 답변 + 사진 100% + 주(週)당 주간 1 + 월(月)당 월간 V2 1.
     * 렌더 peak heap 은 "모든 답변 사진 바이트가 photoAssets 맵에 동시 상주"가 지배하므로 사진비율 1.0 이 상한.
     */
    public static PdfSyntheticData oneYearWorstCase(LocalDate endDate, int days) {
        return of(endDate, days, 1.0);
    }

    /** endDate = 기간 종료일(inclusive), days = 답변 일수(366=최대 1년), photoRatio = 사진 있는 답변 비율(0.0~1.0, 1.0=최악). */
    public static PdfSyntheticData of(LocalDate endDate, int days, double photoRatio) {
        double r = photoRatio < 0 ? 0 : Math.min(1.0, photoRatio);
        return build(endDate, days, (int) Math.round(days * r));
    }

    /** 정확히 photoCount 장을 days 개 답변에 균등 배치(export 상한 사이징 스윕용 — ratio 이산화 없이 정확한 사진 수). */
    public static PdfSyntheticData ofPhotoCount(LocalDate endDate, int days, int photoCount) {
        return build(endDate, days, Math.max(0, Math.min(photoCount, days)));
    }

    private static PdfSyntheticData build(LocalDate endDate, int days, int photoCount) {
        LocalDate startDate = endDate.minusDays(days - 1L);

        // 원본 사진 바이트는 미리(=측정 구간 밖에서) 준비한다. 프로덕션 resolvePhoto 는 S3 에서 이미 존재하는 webp 를 "받아서
        // 디코드"할 뿐 이미지를 생성하지 않으므로, 무거운 준비 비용이 assemble/CPU 측정을 오염시키면 안 된다.
        // 리졸버는 프로덕션과 동일하게 매 호출 디코드→정사각 리샘플→JPEG 재인코딩만 수행한다(실 webp 우선, 없으면 합성 1280² PNG).
        byte[][] variants = resolveVariants();
        Function<String, Optional<byte[]>> photoResolver = key -> {
            try {
                byte[] src = variants[Math.floorMod(key.hashCode(), variants.length)];
                return Optional.of(PdfImage.coverSquareJpegBytes(src, PHOTO_PX));
            } catch (Exception e) {
                return Optional.empty();
            }
        };

        List<PdfAnswerRowDto> answers = buildAnswers(startDate, days, photoCount);
        List<WeeklyReport> weeklies = buildWeeklies(startDate, endDate);
        List<MonthlyReportV2> monthlyV2s = buildMonthlyV2s(startDate, endDate);
        List<MonthlyReport> monthlies = List.of(); // V1(레거시)은 2026-05 이전 잔존분 — 최악 규모엔 비지배라 생략(필요 시 추가)

        return new PdfSyntheticData(answers, weeklies, monthlies, monthlyV2s, photoResolver);
    }

    /**
     * 640×640 q0.82 재인코딩 후 JPEG 1장의 실제 바이트 크기(하네스가 사진 현실성 검증·출력용).
     * asset: 서빙이라 이 바이트가 곧 렌더 중 photoAssets 맵에 상주하는 크기(base64/UTF-16 인플레이션 없음).
     */
    public static int samplePhotoByteSize() {
        return PdfImage.coverSquareJpegBytes(resolveVariants()[0], PHOTO_PX).length;
    }

    /**
     * 각 원본 변이의 640 재인코딩 JPEG 바이트 크기(사진 프리플라이트·현실성 검증용).
     * 디코드 실패 시 PdfImage.coverSquareJpegBytes 가 예외 전파 → 하네스가 "조용한 빈 사진 렌더"를 측정하는 사고를 막는다.
     */
    public static int[] variantPhotoByteSizes() {
        byte[][] variants = resolveVariants();
        int[] sizes = new int[variants.length];
        for (int i = 0; i < variants.length; i++) {
            sizes[i] = PdfImage.coverSquareJpegBytes(variants[i], PHOTO_PX).length;
        }
        return sizes;
    }

    /* ── 사진 원본 결정: 실제 webp 디렉터리 우선, 없으면 합성 폴백 ── */

    private static byte[][] resolveVariants() {
        byte[][] real = loadRealWebps();
        if (real != null && real.length > 0) {
            return real;
        }
        return sourceVariants(SOURCE_VARIANTS);
    }

    /** -Dpdf.bench.photoDir=... 디렉터리의 *.webp 를 원본 변이로 로드(정렬). 미지정/없음/실패 시 null → 합성 폴백. */
    private static byte[][] loadRealWebps() {
        String dir = System.getProperty(PHOTO_DIR_PROP);
        if (dir == null || dir.isBlank()) {
            lastPhotoSourceInfo = "합성 사진(1280² 고주파, %d변이) — 실사진 쓰려면 -D%s=<디렉터리>".formatted(SOURCE_VARIANTS, PHOTO_DIR_PROP);
            return null;
        }
        try (Stream<Path> s = Files.list(Path.of(dir))) {
            List<Path> webps = s.filter(f -> f.getFileName().toString().toLowerCase().endsWith(".webp")).sorted().toList();
            if (webps.isEmpty()) {
                lastPhotoSourceInfo = "합성 폴백(디렉터리에 *.webp 없음: " + dir + ")";
                return null;
            }
            byte[][] out = new byte[webps.size()][];
            for (int i = 0; i < webps.size(); i++) {
                out[i] = Files.readAllBytes(webps.get(i));
            }
            lastPhotoSourceInfo = "실제 webp %d장 (%s)".formatted(webps.size(), dir);
            return out;
        } catch (IOException | RuntimeException e) {
            lastPhotoSourceInfo = "합성 폴백(webp 로드 실패: " + e.getMessage() + ")";
            return null;
        }
    }

    /* ── 답변: 매일 1건, 긴/짧은 본문·관심사·감정 순환, 사진비율 적용 ── */

    private static final InterestCode[] INTERESTS = {
            InterestCode.EMOTION, InterestCode.VALUES, InterestCode.RELATIONSHIP, InterestCode.PREFERENCE
    };
    private static final EmotionCode[] EMOTIONS = {
            EmotionCode.ETC, EmotionCode.WILL, EmotionCode.PEACE, EmotionCode.PLEASURE
    };

    private static final String LONG_BODY =
            "요즘은 하루를 마무리하며 오늘 무엇을 느꼈는지 천천히 되짚어보는 습관을 들이려 한다. "
            + "바쁘게 지나가는 순간들 속에서도 작은 감정 하나하나를 놓치지 않고 기록해두면, "
            + "나중에 돌아봤을 때 그때의 내가 어떤 마음이었는지 더 선명하게 떠올릴 수 있다. "
            + "거창한 사건이 아니어도 괜찮다. 오히려 사소한 순간들이 모여 지금의 나를 만든다고 믿는다.";
    private static final String SHORT_BODY =
            "아침에 마시는 따뜻한 커피 한 잔이 하루의 시작을 기분 좋게 만들어 준다.";

    private static List<PdfAnswerRowDto> buildAnswers(LocalDate startDate, int days, int photoCount) {
        List<PdfAnswerRowDto> list = new ArrayList<>(days);
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            String question = "오늘 하루 중 가장 마음에 남은 순간은 무엇인가요? (" + (i + 1) + "일차)";
            String body = (i % 3 == 0) ? LONG_BODY : SHORT_BODY;
            // 정확히 photoCount 장을 균등 배치(불린 합이 정확히 photoCount).
            boolean hasPhoto = (int) ((long) (i + 1) * photoCount / days) > (int) ((long) i * photoCount / days);
            String imageKey = hasPhoto ? ("answers/bench/photo-" + i + ".webp") : null;
            list.add(new PdfAnswerRowDto(date, body, imageKey, question,
                    INTERESTS[i % INTERESTS.length], EMOTIONS[i % EMOTIONS.length]));
        }
        return list;
    }

    /* ── 주간: 기간 내 월~일 버킷마다 COMPLETED 1건(템플릿 2종 순환) ── */

    private static final String[] WEEKLY_TEMPLATES = {
            """
            {"improve":{"segments":[{"text":"하루 30분 정도 온전히 나를 위한 시간을 미리 정해두고, 그 시간엔 좋아하는 활동 하나만 골라 가볍게 즐겨보는 건 어때요?","marks":[]}]},"summary":"차분히 나를 돌보는 한 주","discovered":{"segments":[{"text":"한 주 동안 익숙하고 안정적인 것을 선호하는 모습이 반복해서 나타났어요. 조용한 환경에서 에너지를 회복하고 계획을 세워 하나씩 정리할 때 마음이 편안해지는 경향이 보여요.","marks":[]}]}}
            """,
            """
            {"improve":{"segments":[{"text":"고마운 마음이 들 때 ","marks":[]},{"text":"짧게라도 바로 표현하기","marks":["BOLD","HIGHLIGHT"]},{"text":"를 작은 목표로 삼아보는 건 어때요?","marks":[]}]},"summary":"고마움을 표현하려는 한 주","discovered":{"segments":[{"text":"가까운 사람들과의 관계에서 안정과 즐거움을 크게 느끼는 모습이 보여요. 다만 마음을 직접 말로 전하는 건 조금 어색해해서 편지나 작은 행동으로 대신 표현하는 선택을 자주 해요.","marks":[]}]}}
            """
    };

    private static List<WeeklyReport> buildWeeklies(LocalDate startDate, LocalDate endDate) {
        List<WeeklyReport> list = new ArrayList<>();
        LocalDate weekStart = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        int idx = 0;
        while (!weekStart.isAfter(endDate)) {
            LocalDate weekEnd = weekStart.plusDays(6);
            try {
                ReportContent content = MAPPER.readValue(WEEKLY_TEMPLATES[idx % WEEKLY_TEMPLATES.length], ReportContent.class);
                list.add(WeeklyReport.create(null, weekStart, weekEnd, content, weekEnd, WeeklyReportStatus.COMPLETED));
            } catch (Exception e) {
                throw new IllegalStateException("주간 합성 데이터 생성 실패", e);
            }
            weekStart = weekStart.plusWeeks(1);
            idx++;
        }
        return list;
    }

    /* ── 월간 V2: 기간 내 매달 COMPLETED 1건(비교 있음 → 레이더 2계열) ── */

    private static final String V2_CONTENT = """
            {"comment":{"segments":[{"text":"스스로에게 높은 기준을 두는 마음이 때로는 부담이 될 수 있어요. 그럴 때는 오늘 해낸 ","marks":[]},{"text":"아주 작은 일이라도 괜찮다고 말해주며","marks":["BOLD","HIGHLIGHT"]},{"text":" 스스로를 인정해주는 건 어때요?","marks":[]}]},"summary":"자신을 돌보며 균형을 찾으려는 노력","discovered":{"segments":[{"text":"마음속으로 ","marks":[]},{"text":"효율과 건강을 중요하게 여기고","marks":["BOLD","HIGHLIGHT"]},{"text":", 스스로에게 높은 기대를 두고 있어요. 그래서 더 잘하고 싶다는 의지가 강하게 나타나고, 꾸준한 습관으로 자신을 돌보며 활력을 유지하려고 해요.","marks":[]}]},"emotionTrend":"의지 키워드를 중심으로 성취와 흥미가 함께 늘었어요.","commentSummary":"작은 노력도 인정하며 편안함을 찾아요","dominantKeyword":"의지"}
            """;
    private static final String V2_STATS = """
            {"emotions":[{"count":6,"percent":30,"emotionCode":"WILL","emotionName":"의지"},{"count":4,"percent":22,"emotionCode":"PEACE","emotionName":"평온"},{"count":3,"percent":15,"emotionCode":"ACHIEVEMENT","emotionName":"성취"},{"count":2,"percent":11,"emotionCode":"ETC","emotionName":"기타"},{"count":1,"percent":6,"emotionCode":"DEPRESSION","emotionName":"우울"},{"count":1,"percent":6,"emotionCode":"INTEREST","emotionName":"흥미"},{"count":1,"percent":5,"emotionCode":"PLEASURE","emotionName":"즐거움"},{"count":1,"percent":5,"emotionCode":"REGRET","emotionName":"후회"}],"totalCount":19,"positivePercent":78,"dominantEmotionCode":"WILL"}
            """;
    private static final String V2_PREV_STATS = """
            {"emotions":[{"count":3,"percent":16,"emotionCode":"WILL","emotionName":"의지"},{"count":5,"percent":28,"emotionCode":"PEACE","emotionName":"평온"},{"count":2,"percent":11,"emotionCode":"ACHIEVEMENT","emotionName":"성취"},{"count":2,"percent":11,"emotionCode":"ETC","emotionName":"기타"},{"count":2,"percent":11,"emotionCode":"DEPRESSION","emotionName":"우울"},{"count":1,"percent":6,"emotionCode":"INTEREST","emotionName":"흥미"},{"count":2,"percent":11,"emotionCode":"PLEASURE","emotionName":"즐거움"},{"count":1,"percent":6,"emotionCode":"REGRET","emotionName":"후회"}],"totalCount":18,"positivePercent":66,"dominantEmotionCode":"PEACE"}
            """;
    private static final String V2_EMOTION_SUMMARY = """
            {"styledText": {"segments": [{"text": "이번 달은 ", "marks": []}, {"text": "의지", "marks": ["BOLD", "HIGHLIGHT"]}, {"text": ", ", "marks": []}, {"text": "평온", "marks": ["BOLD", "HIGHLIGHT"]}, {"text": "이 함께한 한 달이었어요. 자신을 돌보며 목표를 향해 나아가는 의지 속에서 평온을 지키려는 마음이 돋보여요.", "marks": []}]}}
            """;

    private static List<MonthlyReportV2> buildMonthlyV2s(LocalDate startDate, LocalDate endDate) {
        List<MonthlyReportV2> list = new ArrayList<>();
        LocalDate monthStart = startDate.withDayOfMonth(1);
        while (!monthStart.isAfter(endDate)) {
            LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());
            try {
                MonthlyReportV2Content content = MAPPER.readValue(V2_CONTENT, MonthlyReportV2Content.class);
                MonthlyReportV2 m = MonthlyReportV2.create(null, monthStart, monthEnd, content, monthEnd,
                        MonthlyReportStatus.COMPLETED, MonthlyReportImageStatus.COMPLETED,
                        MonthlyReportComparisonType.COMPARISON);
                setField(m, "emotionStats", MAPPER.readValue(V2_STATS, TypeEmotionStatsContent.class));
                setField(m, "emotionComparison", new MonthlyEmotionComparisonContent(
                        null, 5, MAPPER.readValue(V2_PREV_STATS, TypeEmotionStatsContent.class), 12));
                setField(m, "emotionSummaryContent", MAPPER.readValue(V2_EMOTION_SUMMARY, TypeTextContent.class));
                list.add(m);
            } catch (Exception e) {
                throw new IllegalStateException("월간 V2 합성 데이터 생성 실패", e);
            }
            monthStart = monthStart.plusMonths(1);
        }
        return list;
    }

    /* ── 합성 사진 원본: 사진급 고주파(다중 스케일 랜덤 블록 + 픽셀 노이즈) → 640 재인코딩이 실사진 수준 크기가 되도록 ── */

    /** 시드별 원본 변이를 미리 생성(측정 밖에서 1회). */
    private static byte[][] sourceVariants(int count) {
        byte[][] variants = new byte[count][];
        for (int i = 0; i < count; i++) {
            variants[i] = photographicSourceBytes(1000 + i);
        }
        return variants;
    }

    /**
     * 640 으로 다운스케일해도 디테일이 남도록 여러 스케일의 랜덤 블록을 겹쳐 그린 뒤 픽셀 노이즈를 더한다.
     * 단순 그라디언트/단색은 640 q0.82 재인코딩이 수 KB로 뭉개져 peak heap 을 과소평가하므로 의도적으로 "바쁜" 이미지를 만든다.
     * seed 로 키마다 다른 원본을 만들어 640 재인코딩(및 그 결과 크기)이 매번 실제로 달라지게 한다.
     */
    private static byte[] photographicSourceBytes(int seed) {
        int w = SOURCE_SIDE;
        int h = SOURCE_SIDE; // PDF 가 받는 answer webp 는 앱이 1280×1280 정사각으로 올린 것 → 640 재인코딩은 순수 다운스케일(프로덕션 동일)
        Random rnd = new Random(seed);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        try {
            // 1) 부드러운 배경(저주파)
            g.setColor(new Color(120 + rnd.nextInt(80), 120 + rnd.nextInt(80), 120 + rnd.nextInt(80)));
            g.fillRect(0, 0, w, h);
            // 2) 다중 스케일 랜덤 블록(중주파 — 640 다운스케일 후에도 에지가 남아 JPEG 크기를 실사진 수준으로 유지)
            int[] blockSizes = {160, 80, 40, 20};
            for (int bs : blockSizes) {
                for (int i = 0, n = (w * h) / (bs * bs) * 2; i < n; i++) {
                    g.setColor(new Color(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)));
                    int x = rnd.nextInt(w), y = rnd.nextInt(h);
                    g.fillRect(x, y, bs, bs);
                }
            }
        } finally {
            g.dispose();
        }
        // 3) 픽셀 노이즈(고주파 — 균일 영역을 없애 압축률을 사진처럼 떨어뜨림)
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int r = clamp(((rgb >> 16) & 0xFF) + rnd.nextInt(41) - 20);
                int gc = clamp(((rgb >> 8) & 0xFF) + rnd.nextInt(41) - 20);
                int b = clamp((rgb & 0xFF) + rnd.nextInt(41) - 20);
                img.setRGB(x, y, (r << 16) | (gc << 8) | b);
            }
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(img, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("합성 사진 생성 실패", e);
        }
    }

    private static int clamp(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private PdfSyntheticData() { throw new AssertionError(); }
}
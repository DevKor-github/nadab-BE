package com.devkor.ifive.nadab.domain.pdfexport.application.render;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionCode;
import com.devkor.ifive.nadab.domain.monthlyreport.core.content.MonthlyEmotionComparisonContent;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReport;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportComparisonType;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportImageStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportStatus;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2Content;
import com.devkor.ifive.nadab.domain.pdfexport.core.dto.PdfAnswerRowDto;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportType;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeTextContent;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReport;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReportStatus;
import com.devkor.ifive.nadab.global.core.pdf.PdfAssetLoader;
import com.devkor.ifive.nadab.global.shared.reportcontent.ReportContent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 렌더 파이프라인 스모크 — DB/S3 없이 합성 샘플 데이터로 실제 assemble→render 경로를 태워 유효한 PDF 바이트가 나오는지 검증한다.
 *
 * 원칙: 렌더 로직은 프로덕션 코드(어셈블러·PdfImage 등)에 있고 테스트는 "날것 데이터만 주입"한다.
 *  - 리포트 content/emotion_stats: jsonb 문자열을 Jackson 역직렬화 = 프로덕션 Hibernate 엔티티화와 동일(손 전사 오류 방지).
 *  - 답변 사진: 날것 이미지 바이트를 주입하고, 정사각 cover 크롭/리샘플은 프로덕션 PdfImage 가 수행.
 */
class PdfPreviewTest {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /** 답변 사진 임베드 한 변 px(표시 ~431px의 오버샘플). */
    private static final int PHOTO_PX = 640;

    @Test
    void 샘플데이터로_전체_렌더가_유효한_PDF를_만든다() throws Exception {
        PdfAssetLoader assets = new PdfAssetLoader();
        invoke(assets, "load");
        EmotionRadarChartRenderer radar = new EmotionRadarChartRenderer();
        PdfShadowRenderer shadow = new PdfShadowRenderer();
        PdfHtmlAssembler assembler = new PdfHtmlAssembler(assets, radar, shadow);
        invoke(assembler, "loadCss");
        PdfRenderer renderer = new PdfRenderer(assets, shadow);

        byte[] samplePhoto = samplePhotoBytes();
        Function<String, Optional<byte[]>> photoResolver = key -> Optional.of(samplePhoto);

        PdfHtmlAssembler.AssembledDocument doc = assembler.assemble(
                PdfExportType.REPORT_AND_ANSWER,
                sampleAnswers(),
                sampleWeeklies(),
                sampleMonthlies(),
                sampleMonthlyV2s(),
                photoResolver);

        Path pdfFile = renderer.render(doc.xhtml(), doc.inlineAssets());

        // 전체 파이프라인이 예외 없이 돌고 유효한 PDF 헤더의 파일을 만든다(디자인 충실도가 아니라 "렌더가 깨지지 않음"을 지킴).
        try {
            assertThat(Files.size(pdfFile)).isGreaterThan(1000);
            byte[] header = new byte[5];
            try (InputStream in = Files.newInputStream(pdfFile)) {
                in.readNBytes(header, 0, 5);
            }
            assertThat(new String(header, StandardCharsets.ISO_8859_1)).startsWith("%PDF-");
        } finally {
            Files.deleteIfExists(pdfFile);
        }
    }

    /* ── 답변 (합성 5건 — 긴/짧은·사진 有/無·관심사 "관계" 커버) ── */

    private List<PdfAnswerRowDto> sampleAnswers() {
        return List.of(
                ans("2026-01-02", "요즘 가장 자주 떠오르는 생각은 무엇인가요?",
                        "요즘은 새해가 시작되면서 올 한 해를 어떻게 보낼지 자주 생각하게 된다. 작년에는 계획만 세우고 실천을 잘 못한 것 같아서 올해는 조금 더 구체적으로 목표를 나눠보려고 한다. 거창한 목표보다 매일 조금씩 할 수 있는 작은 습관부터 만들어가는 게 오래 갈 것 같다는 생각이 든다. 그렇게 하나씩 쌓다 보면 연말에는 꽤 달라져 있지 않을까 기대가 된다.",
                        true, InterestCode.EMOTION, EmotionCode.ETC),
                ans("2026-01-12", "내 하루에 가장 큰 활력을 주는 건 무엇인가요?",
                        "아침에 마시는 따뜻한 커피 한 잔이 하루의 시작을 기분 좋게 만들어 준다.",
                        false, InterestCode.EMOTION, EmotionCode.ETC),
                ans("2026-01-14", "나에게 '꾸준함'이란 어떤 의미인가요?",
                        "나에게 꾸준함이란 결과보다 과정을 믿는 태도인 것 같다. 매일 같은 자리에서 조금씩 반복하다 보면 그 시간이 결국 힘이 된다고 믿는다. 하루하루는 티가 잘 나지 않지만, 뒤돌아보면 그 작은 반복들이 지금의 나를 만들었다는 생각이 든다.",
                        true, InterestCode.VALUES, EmotionCode.WILL),
                ans("2026-02-09", "나를 편하게 해주는 사람은 어떤 사람인가요?",
                        "말없이 함께 있어도 어색하지 않은 사람이 제일 편하다. 무언가를 억지로 채우려 하지 않고 그냥 옆에 있어 주는 사람과 있으면 마음이 놓인다.",
                        false, InterestCode.RELATIONSHIP, EmotionCode.PEACE),
                ans("2026-02-15", "겨울에 가장 좋아하는 것은 무엇인가요?",
                        "겨울에는 따뜻한 방에서 귤을 까먹는 순간이 제일 좋다. 소소하지만 확실한 행복이다.",
                        true, InterestCode.PREFERENCE, EmotionCode.PLEASURE)
        );
    }

    private PdfAnswerRowDto ans(String date, String question, String content,
                                boolean photo, InterestCode interest, EmotionCode emotion) {
        return new PdfAnswerRowDto(LocalDate.parse(date), content, photo ? "sample-photo" : null,
                question, interest, emotion);
    }

    /* ── 주간 리포트 (합성 3건 — 마크 有/無 커버, content jsonb 그대로 파싱) ── */

    private List<WeeklyReport> sampleWeeklies() throws Exception {
        return List.of(
                weekly("2025-12-29", "2026-01-04", """
                        {"improve":{"segments":[{"text":"하루 30분 정도 온전히 나를 위한 시간을 미리 정해두고, 그 시간엔 좋아하는 활동 하나만 골라 가볍게 즐겨보는 건 어때요? 작은 여유가 한 주를 부드럽게 만들어 줘요.","marks":[]}]},"summary":"새해를 차분히 여는 한 주","discovered":{"segments":[{"text":"한 주 동안 큰 자극보다 익숙하고 안정적인 것을 선호하는 모습이 반복해서 나타났어요. 조용한 환경에서 에너지를 회복하고 계획을 세워 하나씩 정리할 때 마음이 편안해지는 경향이 보여요. 새로운 시도 앞에서는 잠시 망설이지만 준비가 되면 차분히 실행으로 옮기는 흐름이 이어졌어요.","marks":[]}]}}
                        """),
                weekly("2026-01-12", "2026-01-18", """
                        {"improve":{"segments":[{"text":"고마운 마음이 들 때 ","marks":[]},{"text":"짧게라도 바로 표현하기","marks":["BOLD","HIGHLIGHT"]},{"text":"를 작은 목표로 삼아보는 건 어때요? 한 번의 표현이 다음 표현을 훨씬 쉽게 만들어 줘요.","marks":[]}]},"summary":"고마움을 표현하려는 한 주","discovered":{"segments":[{"text":"가까운 사람들과의 관계에서 안정과 즐거움을 크게 느끼는 모습이 보여요. 다만 마음을 직접 말로 전하는 건 조금 어색해해서 편지나 작은 행동으로 대신 표현하는 선택을 자주 해요. 이런 방식이 반복될수록 부끄러움이 줄고 자연스러운 표현으로 이어지는 힘이 돼요.","marks":[]}]}}
                        """),
                weekly("2026-01-19", "2026-01-25", """
                        {"improve":{"segments":[{"text":"소중하게 느껴지는 물건이나 순간에 짧은 메모를 붙여두고, 비슷한 메모를 모아 간단한 목록으로 정리해보는 건 어때요? 쌓인 기록이 변화를 확인하는 즐거움을 줘요.","marks":[]}]},"summary":"의미 있는 것을 곁에 두는 한 주","discovered":{"segments":[{"text":"지속성과 의미를 중요하게 여기는 마음이 과거의 경험에 특히 민감하게 반응해요. 그래서 추억이 담긴 물건을 쉽게 정리하지 못하고 여행이나 기록처럼 오래 남는 것에 시간을 쓰는 선택을 하게 돼요. 이런 반복은 지나온 흔적을 통해 자기 신뢰를 확인시켜 주기 때문에 자연스럽게 이어져요.","marks":[]}]}}
                        """)
        );
    }

    private WeeklyReport weekly(String start, String end, String contentJson) throws Exception {
        ReportContent content = MAPPER.readValue(contentJson, ReportContent.class);
        return WeeklyReport.create(null, LocalDate.parse(start), LocalDate.parse(end),
                content, LocalDate.parse(end), WeeklyReportStatus.COMPLETED);
    }

    /* ── 월간 리포트 V1(레거시, 2026-05 이전) — 감정/레이더 없이 주간과 동일한 텍스트 카드로 렌더 ── */

    private List<MonthlyReport> sampleMonthlies() throws Exception {
        ReportContent content = MAPPER.readValue("""
                {"improve":{"segments":[{"text":"한 주에 한 번은 ","marks":[]},{"text":"혼자만의 시간을 미리 정해두고","marks":["BOLD","HIGHLIGHT"]},{"text":", 그 시간엔 좋아하는 활동 하나만 골라 온전히 누려보는 건 어때요?","marks":[]}]},"summary":"편안함 속에서 나를 지키려는 한 달","discovered":{"segments":[{"text":"한 달 동안 ","marks":[]},{"text":"마음의 안정과 소중한 관계","marks":["BOLD","HIGHLIGHT"]},{"text":"를 중요하게 여기는 모습이 반복해서 드러났어요. 혼자만의 시간으로 에너지를 회복하고, 가까운 사람에게 있는 그대로 받아들여지는 편안함을 느끼며, 그 안정감을 바탕으로 한 걸음씩 나아가려는 흐름이 이어졌어요. 익숙한 것을 선호하면서도 스스로에게 도움이 되는 선택을 꾸준히 이어가려는 의지가 함께 보여요.","marks":[]}]}}
                """, ReportContent.class);

        MonthlyReport m = MonthlyReport.create(null, LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-31"),
                content, LocalDate.parse("2026-01-31"), MonthlyReportStatus.COMPLETED);
        return List.of(m);
    }

    /* ── 월간 리포트 V2 (합성 1건 — 비교 있음(COMPARISON)이라 레이더 2계열 + 부제·월 라벨) ── */

    private List<MonthlyReportV2> sampleMonthlyV2s() throws Exception {
        MonthlyReportV2Content content = MAPPER.readValue("""
                {"comment":{"segments":[{"text":"스스로에게 높은 기준을 두는 마음이 때로는 부담이 될 수 있어요. 그럴 때는 오늘 해낸 ","marks":[]},{"text":"아주 작은 일이라도 괜찮다고 말해주며","marks":["BOLD","HIGHLIGHT"]},{"text":" ","marks":[]},{"text":"스스로를 인정해주는 건 어때요","marks":["BOLD","HIGHLIGHT"]},{"text":"? 잠시 멈춰 ","marks":[]},{"text":"지금의 편안함에 집중하고","marks":["BOLD","HIGHLIGHT"]},{"text":" ","marks":[]},{"text":"작은 휴식으로 다시 나아갈 힘을 얻어도 괜찮아요","marks":["BOLD","HIGHLIGHT"]},{"text":".","marks":[]}]},"summary":"자신을 돌보며 균형을 찾으려는 노력","discovered":{"segments":[{"text":"마음속으로 ","marks":[]},{"text":"효율과 건강을 중요하게 여기고","marks":["BOLD","HIGHLIGHT"]},{"text":", ","marks":[]},{"text":"스스로에게 높은 기대를 두고 있어요","marks":["BOLD","HIGHLIGHT"]},{"text":". 그래서 더 잘하고 싶다는 의지가 강하게 나타나고, ","marks":[]},{"text":"꾸준한 습관으로 자신을 돌보며","marks":["BOLD","HIGHLIGHT"]},{"text":" 활력을 유지하려고 해요. 다만 때로는 기준이 엄격해 스스로의 노력을 충분히 인정하지 못하고, 약속이 느슨해지면 후회하기도 해요. 익숙하지 않은 상황에서 오는 긴장을 피하고 싶어 주저하는 경우도 있어요. 이런 마음들은 편안함을 바라면서도 ","marks":[]},{"text":"잘 해내고 싶은 기대","marks":["BOLD","HIGHLIGHT"]},{"text":"와 맞닿아 반복되는 것 같아요.","marks":[]}]},"emotionTrend":"의지 키워드를 중심으로 성취와 흥미가 함께 늘었어요.","commentSummary":"작은 노력도 인정하며 편안함을 찾아요","dominantKeyword":"의지"}
                """, MonthlyReportV2Content.class);

        MonthlyReportV2 m = MonthlyReportV2.create(null, LocalDate.parse("2026-06-01"), LocalDate.parse("2026-06-30"),
                content, LocalDate.parse("2026-06-30"), MonthlyReportStatus.COMPLETED,
                MonthlyReportImageStatus.COMPLETED, MonthlyReportComparisonType.COMPARISON);

        TypeEmotionStatsContent stats = MAPPER.readValue("""
                {"emotions":[{"count":6,"percent":30,"emotionCode":"WILL","emotionName":"의지"},{"count":4,"percent":22,"emotionCode":"PEACE","emotionName":"평온"},{"count":3,"percent":15,"emotionCode":"ACHIEVEMENT","emotionName":"성취"},{"count":2,"percent":11,"emotionCode":"ETC","emotionName":"기타"},{"count":1,"percent":6,"emotionCode":"DEPRESSION","emotionName":"우울"},{"count":1,"percent":6,"emotionCode":"INTEREST","emotionName":"흥미"},{"count":1,"percent":5,"emotionCode":"PLEASURE","emotionName":"즐거움"},{"count":1,"percent":5,"emotionCode":"REGRET","emotionName":"후회"}],"totalCount":19,"positivePercent":78,"dominantEmotionCode":"WILL"}
                """, TypeEmotionStatsContent.class);
        setField(m, "emotionStats", stats);

        // 테스트용 지난 달 비교 데이터(2계열 레이더 + 부제·월 라벨 검증). 실제 BASELINE 이면 null → 단일 계열.
        TypeEmotionStatsContent prevStats = MAPPER.readValue("""
                {"emotions":[{"count":3,"percent":16,"emotionCode":"WILL","emotionName":"의지"},{"count":5,"percent":28,"emotionCode":"PEACE","emotionName":"평온"},{"count":2,"percent":11,"emotionCode":"ACHIEVEMENT","emotionName":"성취"},{"count":2,"percent":11,"emotionCode":"ETC","emotionName":"기타"},{"count":2,"percent":11,"emotionCode":"DEPRESSION","emotionName":"우울"},{"count":1,"percent":6,"emotionCode":"INTEREST","emotionName":"흥미"},{"count":2,"percent":11,"emotionCode":"PLEASURE","emotionName":"즐거움"},{"count":1,"percent":6,"emotionCode":"REGRET","emotionName":"후회"}],"totalCount":18,"positivePercent":66,"dominantEmotionCode":"PEACE"}
                """, TypeEmotionStatsContent.class);
        MonthlyEmotionComparisonContent comparison =
                new MonthlyEmotionComparisonContent(null, 5, prevStats, 12);
        setField(m, "emotionComparison", comparison);

        // 감정 요약(레이더 박스 안 문구) = 실제 emotionSummaryContent 스타일드 텍스트.
        TypeTextContent emotionSummary = MAPPER.readValue("""
                {"styledText": {"segments": [{"text": "이번 달은 ", "marks": []}, {"text": "의지", "marks": ["BOLD", "HIGHLIGHT"]}, {"text": ", ", "marks": []}, {"text": "평온", "marks": ["BOLD", "HIGHLIGHT"]}, {"text": ", ", "marks": []}, {"text": "성취", "marks": ["BOLD", "HIGHLIGHT"]}, {"text": "가 함께한 한 달이었어요. 자신을 돌보며 ", "marks": []}, {"text": "목표를 향해 나아가는 의지", "marks": ["BOLD", "HIGHLIGHT"]}, {"text": " 속에서 평온을 지키려는 마음이 돋보여요.", "marks": []}]}}
                """, TypeTextContent.class);
        setField(m, "emotionSummaryContent", emotionSummary);

        return List.of(m);
    }

    /* ── 샘플 사진: 파일 없이 코드로 만든 합성 사진을 프로덕션 PdfImage(정사각 cover 크롭)로 태운다 ── */

    private byte[] samplePhotoBytes() throws Exception {
        return PdfImage.coverSquareJpegBytes(syntheticPhotoBytes(), PHOTO_PX);
    }

    /** 비정사각 800×600 그라디언트 → cover-crop 경로까지 태우는 합성 사진 바이트(레포에 바이너리 파일을 두지 않는다). */
    private static byte[] syntheticPhotoBytes() throws Exception {
        BufferedImage img = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setPaint(new GradientPaint(0, 0, new Color(0x5D57F6), 800, 600, new Color(0xB5E7FF)));
            g.fillRect(0, 0, 800, 600);
        } finally {
            g.dispose();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }

    /* ── 리플렉션(패키지-프라이빗 @PostConstruct 호출 / 세터 없는 필드 주입) ── */

    private static void invoke(Object bean, String method) throws Exception {
        Method m = bean.getClass().getDeclaredMethod(method);
        m.setAccessible(true);
        m.invoke(bean);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
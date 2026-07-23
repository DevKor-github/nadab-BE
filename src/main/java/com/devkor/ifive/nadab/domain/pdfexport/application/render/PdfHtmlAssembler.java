package com.devkor.ifive.nadab.domain.pdfexport.application.render;

import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReport;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2;
import com.devkor.ifive.nadab.domain.monthlyreport.core.entity.MonthlyReportV2Content;
import com.devkor.ifive.nadab.domain.pdfexport.core.dto.PdfAnswerRowDto;
import com.devkor.ifive.nadab.domain.pdfexport.core.entity.PdfExportType;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent;
import com.devkor.ifive.nadab.domain.typereport.core.content.TypeEmotionStatsContent.EmotionStat;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.weeklyreport.core.entity.WeeklyReport;
import com.devkor.ifive.nadab.global.core.pdf.PdfAssetLoader;
import com.devkor.ifive.nadab.global.shared.reportcontent.ReportContent;
import com.devkor.ifive.nadab.global.shared.reportcontent.StyledText;
import com.devkor.ifive.nadab.global.shared.util.WeekRangeCalculator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 기간 데이터 → 내용맞춤 셀 패킹 → XHTML(openhtmltopdf 입력).
 * 카드 높이 = 내용 줄 수 기반 추정(Java가 높이를 알아야 baked 그림자 작동) → 절대배치, 열 우선 채움.
 */
@Component
@RequiredArgsConstructor
public class PdfHtmlAssembler {

    private static final int MARGIN_X = 30;
    private static final int MARGIN_TOP = 46;
    private static final int COL_W = 463;
    private static final int COL_GAP = 60;
    private static final int COL_H = 1360;   // 콘텐츠 영역(덩어리1) 높이
    private static final int CARD_GAP = 40;  // 카드 세로 간격(일정)
    private static final int COLS = 2;

    /** 추정용 내부 폭(px): 카드 내부 429 = 463 - border2 - padding32. 흰 박스 내부 395 = 429 - border2 - padding32. */
    private static final int CARD_INNER = 429;
    private static final int WHITE_INNER = 395;

    /* ── 리포트 카드 높이 추정 상수(내용맞춤 셀 절대배치용) ── */
    /** 리포트 고정 오버헤드: border 2 + 배너 163 + padding-bottom 16(배너가 상단 padding 을 덮음). */
    private static final int REPORT_BASE = 181;
    /** 리포트 본문 줄높이(14px * 1.8 ≈ 25.2 → 26, 약간의 여유). */
    private static final int REPORT_BODY_LH = 26;
    /** 발견/레이더 흰 박스 오버헤드: border 2 + padding 32. */
    private static final int BOX_OVERHEAD = 34;
    /** 한 마디 박스 오버헤드: 테두리 없음 + padding 상하 8(=16). Figma: 테두리 X, padding 8/16. */
    private static final int COMMENT_BOX_OVERHEAD = 16;
    /** 한 마디 박스 사이 간격 = 박스와 양옆 사이드 여백(카드 padding 16)과 동일. */
    private static final int COMMENT_BOX_GAP = 16;
    /** 섹션 head: 라인 24 + 하단 margin 8. */
    private static final int SECTION_HEAD_H = 32;
    /** 배너 아래 블록들(발견·한마디·부제+레이더 묶음) 사이 간격 — Figma 32. */
    private static final int SECTION_GAP = 32;
    /** 부제(emotionTrend) → 레이더 박스 간격 = head→box(8)과 동일(사용자 요청). */
    private static final int SUBTITLE_RADAR_GAP = 8;
    /** 레이더 박스 안: 월 라벨 아래 문구와의 간격. */
    private static final int RADAR_NOTE_GAP = 12;
    /** 배너 → 첫 블록 간격: 주간=섹션 간격(32)과 동일(배너↔발견 = 발견박스↔한마디). 월간=배너↔부제 24. */
    private static final int WEEKLY_BANNER_GAP = 32;
    private static final int MONTHLY_BANNER_GAP = 24;
    /** 레이더 흰 박스(문구 제외): 박스 오버헤드 34 + 차트 240 + gap 12 + 월 라벨 20. */
    private static final int RADAR_BOX_BASE_H = 34 + 240 + 12 + 20;
    /** 부제(월간 emotionTrend) 줄높이(Bold 14px / line-height 24). */
    private static final int SUBTITLE_LH = 24;

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yy년 M월 d일");
    /** 리포트 kicker 의 "YY년 M월" — 답변 날짜(yy년…)와 2자리 연도로 일관. */
    private static final DateTimeFormatter KICKER_MONTH = DateTimeFormatter.ofPattern("yy년 M월");

    private final PdfAssetLoader assets;
    private final EmotionRadarChartRenderer radarRenderer;
    private final PdfShadowRenderer shadowRenderer;

    private String css;

    /** 줄바꿈 추정을 실제 렌더와 맞추기 위한 임베드 폰트(Pretendard) 메트릭. */
    private final FontRenderContext frc = new FontRenderContext(null, true, true);
    private final Map<Font, Map<Character, Double>> advCache = new ConcurrentHashMap<>();
    private Font qFont;       // 질문 Bold 20px
    private Font bodyFont;    // 답변·리포트 본문 Regular 14px

    @PostConstruct
    void loadCss() {
        try {
            css = new ClassPathResource("pdf/pdf.css").getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("pdf.css 로드 실패", e);
        }
        // HIGHLIGHT 형광펜 바 PNG data URI 주입(정적 CSS 에는 런타임 애셋을 못 박으므로 플레이스홀더 치환).
        css = css.replace("__HL_BAR__", assets.highlightBar());
        loadFonts();
    }

    /** 줄 수 추정용 폰트 로드. 실제 임베드 폰트와 동일 글리프 advance 를 써서 openhtmltopdf 줄바꿈과 일치시킨다. */
    private void loadFonts() {
        try (InputStream reg = new ClassPathResource("fonts/Pretendard-Regular.ttf").getInputStream();
             InputStream bold = new ClassPathResource("fonts/Pretendard-Bold.ttf").getInputStream()) {
            Font regular = Font.createFont(Font.TRUETYPE_FONT, reg);
            Font boldFont = Font.createFont(Font.TRUETYPE_FONT, bold);
            bodyFont = regular.deriveFont(14f);
            qFont = boldFont.deriveFont(20f);
        } catch (Exception e) {
            throw new IllegalStateException("Pretendard 폰트 로드 실패(줄 수 추정용)", e);
        }
    }

    /**
     * 기간 데이터 → XHTML + 사진 인라인 애셋 맵(asset:photo-N → JPEG 바이트). 렌더러가 맵을 받아 asset: 프로토콜로 서빙.
     * photoResolver = imageKey → 사진 바이트(없으면 empty), S3 접점은 리스너가 주입.
     */
    public AssembledDocument assemble(PdfExportType type,
                                      List<PdfAnswerRowDto> answers,
                                      List<WeeklyReport> weeklies,
                                      List<MonthlyReport> monthlies,
                                      List<MonthlyReportV2> monthlyV2s,
                                      Function<String, Optional<byte[]>> photoResolver) {
        // 답변 사진은 asset:photo-N 토큰으로 참조하고 바이트는 이 맵에 모은다(XHTML 엔 토큰만, base64 인라인 안 함).
        // 로컬 맵이라 동시 assemble 도 안전.
        Map<String, byte[]> photoAssets = new LinkedHashMap<>();

        // 시간순 인터리브: 그 주의 답변들 → 그 주 주간 리포트 → 다음 주 답변들 → … → 그 달 월간 리포트.
        // 정렬키 = (날짜, tier). 답변=자기 날짜(tier0), 주간=주 종료일(tier1, 그 주 답변 뒤), 월간=월 종료일(tier2, 그 달 끝).
        List<Sortable> items = new ArrayList<>();
        if (type.includesAnswer()) {
            for (PdfAnswerRowDto a : answers) {
                items.add(new Sortable(a.date(), 0, answerBlock(a, photoResolver, photoAssets)));
            }
        }
        if (type.includesReport()) {
            for (WeeklyReport w : weeklies) {
                items.add(new Sortable(w.getWeekEndDate(), 1, weeklyBlock(w)));
            }
            // 월간 V1(레거시)·V2 모두 tier2(그 달 끝). 한 달 한 버전이라 같은 monthEndDate에 둘이 겹치지 않음.
            for (MonthlyReport m : monthlies) {
                items.add(new Sortable(m.getMonthEndDate(), 2, monthlyBlock(m)));
            }
            for (MonthlyReportV2 m : monthlyV2s) {
                items.add(new Sortable(m.getMonthEndDate(), 2, monthlyV2Block(m)));
            }
        }
        items.sort(Comparator.comparing(Sortable::date).thenComparingInt(Sortable::tier));

        List<Block> blocks = new ArrayList<>(items.size());
        for (Sortable s : items) {
            blocks.add(s.block());
        }
        return new AssembledDocument(document(pack(blocks)), photoAssets);
    }

    /* ── 패킹(내용맞춤 셀, 열 채움) ── */

    private List<List<Positioned>> pack(List<Block> blocks) {
        List<List<Positioned>> pages = new ArrayList<>();
        List<Positioned> page = new ArrayList<>();
        pages.add(page);
        int col = 0;
        int used = 0;

        for (Block b : blocks) {
            int cellH = b.estH();
            if (used > 0 && used + CARD_GAP + cellH > COL_H) {
                col++;
                used = 0;
                if (col >= COLS) {
                    page = new ArrayList<>();
                    pages.add(page);
                    col = 0;
                }
            }
            int x = MARGIN_X + col * (COL_W + COL_GAP);
            int y = MARGIN_TOP + (used == 0 ? 0 : used + CARD_GAP);
            page.add(new Positioned(x, y, COL_W, cellH, b.html()));
            used = (used == 0) ? cellH : used + CARD_GAP + cellH;
        }
        return pages;
    }

    /* ── 블록(카드) ── */

    private Block answerBlock(PdfAnswerRowDto a, Function<String, Optional<byte[]>> photoResolver,
                              Map<String, byte[]> photoAssets) {
        Optional<byte[]> photo = (a.imageKey() == null) ? Optional.empty() : photoResolver.apply(a.imageKey());
        boolean hasPhoto = photo.isPresent();

        StringBuilder tags = new StringBuilder();
        if (a.interestCode() != null) {
            String icon = assets.interestIcon(a.interestCode().name())
                    .map(u -> "<img src=\"" + u + "\" alt=\"\"/>").orElse("");
            // 관심사 라벨. RELATIONSHIP만 "관계"(백엔드 displayNameKo·DB 시드는 "인간관계") → PDF만 앱과 맞춤.
            String interestLabel = a.interestCode() == InterestCode.RELATIONSHIP ? "관계" : a.interestCode().displayNameKo();
            tags.append("<span class=\"tag\">").append(icon)
                    .append("<span class=\"label\">").append(PdfHtml.escape(interestLabel))
                    .append("</span></span>");
        }
        if (a.emotionCode() != null) {
            PdfEmotionPalette.Style st = PdfEmotionPalette.of(a.emotionCode());
            tags.append("<span class=\"tag\"><span class=\"dot\" style=\"background:").append(st.colorHex())
                    .append("\"></span><span class=\"label\">").append(PdfHtml.escape(st.label()))
                    .append("</span></span>");
        }

        // 사진 = wrapper 없는 img(자체 border-radius 네이티브 클리핑). width/height 를 WHITE_INNER 로 명시해
        // 정사각 블록으로 확정(inline-replaced phantom 여백 제거). PdfImage 가 항상 정사각으로 구워 비율 일치.
        // 바이트는 asset:photo-N 으로 참조하고 photoAssets 에 등록. 키는 등록 순번이라 고유.
        String photoHtml = "";
        if (hasPhoto) {
            String assetKey = "photo-" + photoAssets.size();
            photoAssets.put(assetKey, photo.get());
            photoHtml = "<img class=\"a-photo\" width=\"" + WHITE_INNER + "\" height=\"" + WHITE_INNER
                    + "\" src=\"asset:" + assetKey + "\" alt=\"\"/>";
        }
        // 본문↔사진 간격은 본문 블록의 margin-bottom 으로 준다(wrapper margin-top 은 openhtmltopdf 가 드롭).
        String bodyClass = hasPhoto ? "a-body has-photo" : "a-body";

        // 사진은 흰 답변 박스 '안쪽'(내용 아래)에 위치 — Figma 스펙(width 399 = 흰박스 내부폭).
        String html = "<div class=\"a-head\"><span class=\"date\">" + PdfHtml.escape(DATE.format(a.date())) + "</span>"
                + tags + "</div>"
                + "<div class=\"q-title\">" + PdfHtml.escape(a.questionText()) + "</div>"
                + "<div class=\"a-answer\"><div class=\"a-label\">나의 답변</div>"
                + "<div class=\"" + bodyClass + "\">" + PdfHtml.escape(a.content()) + "</div>" + photoHtml + "</div>";

        // 높이 추정: 카드(border+pad 34) + 헤더블록 + gap16(a-answer margin-top) + 흰박스
        int headH = 28 + 8 + textLines(a.questionText(), qFont, CARD_INNER) * 28;
        int bodyH = textLines(a.content(), bodyFont, WHITE_INNER) * 26;   // 본문 줄 수에 딱 맞춤
        int whiteH = 34 + 25 + 16 + bodyH + (hasPhoto ? 16 + WHITE_INNER : 0);
        int estH = 34 + headH + 16 + whiteH;
        return new Block(estH, html);
    }

    private Block weeklyBlock(WeeklyReport w) {
        // kicker = "YY년 M월 N주차 리포트" — 주차는 앱 공용 규칙(WeekRangeCalculator, 월요일 시작·그 주 월요일이 속한 달), 연도·월은 weekStart 기준.
        int wom = WeekRangeCalculator.getWeekOfMonth(WeekRangeCalculator.weekRangeOf(w.getWeekStartDate()));
        String kicker = KICKER_MONTH.format(w.getWeekStartDate()) + " " + wom + "주차 리포트";
        return textReportBlock(kicker, w.getContent());
    }

    /**
     * 월간 V1(레거시) 리포트 카드. V1은 감정 stats·비교·레이더가 없어 필드 구조가 주간과 동일(discovered/improve/summary/content)
     * → 주간과 같은 텍스트 카드로 렌더하고 kicker 만 월간 포맷("YY년 M월 리포트", V2와 통일).
     */
    private Block monthlyBlock(MonthlyReport m) {
        String kicker = KICKER_MONTH.format(m.getMonthStartDate()) + " 리포트";
        return textReportBlock(kicker, m.getContent());
    }

    /** 텍스트 전용 리포트 카드(배너 + 발견 + 한 마디, 레이더 없음). 주간·월간 V1 공용. */
    private Block textReportBlock(String kicker, ReportContent c) {
        String title = c == null ? "" : c.summary();
        StyledText discovered = c == null ? null : c.discovered();
        StyledText improve = c == null ? null : c.improve();

        List<String> blocks = new ArrayList<>();
        List<Integer> heights = new ArrayList<>();
        addBlock(blocks, heights, discoveredSection(discovered), discoveredHeight(plain(discovered)));
        addBlock(blocks, heights, commentSection(improve), commentHeight(improve));

        String html = banner(kicker, title) + joinBlocks(blocks, WEEKLY_BANNER_GAP);
        return new Block(REPORT_BASE + blocksHeight(heights, WEEKLY_BANNER_GAP), html);
    }

    private Block monthlyV2Block(MonthlyReportV2 m) {
        MonthlyReportV2Content c = m.getContent();
        String kicker = KICKER_MONTH.format(m.getMonthStartDate()) + " 리포트";
        String title = c == null ? "" : c.summary();
        String subtitle = c == null ? "" : c.emotionTrend();
        StyledText discovered = c == null ? null : c.discovered();
        StyledText comment = c == null ? null : c.comment();

        // emotionTrend 는 문장이거나 "NOT_SUPPORTED" 센티넬(비교 불가 월) — 센티넬/공백이면 부제 생략.
        boolean hasSubtitle = subtitle != null && !subtitle.isBlank() && !"NOT_SUPPORTED".equals(subtitle);

        List<String> blocks = new ArrayList<>();
        List<Integer> heights = new ArrayList<>();

        // 부제(emotionTrend) + 레이더 박스는 한 묶음 — 부제↔레이더는 head→box 처럼 8px, 묶음 앞은 배너 gap.
        String subtitleHtml = hasSubtitle
                ? "<div class=\"r-subtitle\">" + PdfHtml.escape(subtitle) + "</div>" : "";
        String radarHtml = radar(m);
        String pair = subtitleHtml;
        int pairH = hasSubtitle ? textLines(subtitle, bodyFont, CARD_INNER) * SUBTITLE_LH : 0;
        if (!radarHtml.isEmpty()) {
            pair += hasSubtitle ? "<div style=\"margin-top:" + SUBTITLE_RADAR_GAP + "px\">" + radarHtml + "</div>"
                    : radarHtml;
            pairH += (hasSubtitle ? SUBTITLE_RADAR_GAP : 0)
                    + radarBoxHeight(m.getEmotionSummaryContent() == null ? null : m.getEmotionSummaryContent().plainText());
        }
        addBlock(blocks, heights, pair, pairH);
        addBlock(blocks, heights, discoveredSection(discovered), discoveredHeight(plain(discovered)));
        addBlock(blocks, heights, commentSection(comment), commentHeight(comment));

        String html = banner(kicker, title) + joinBlocks(blocks, MONTHLY_BANNER_GAP);
        return new Block(REPORT_BASE + blocksHeight(heights, MONTHLY_BANNER_GAP), html);
    }

    /** 비어있지 않은 블록만 html·높이 리스트에 함께 추가(둘의 인덱스 일치 보장). */
    private void addBlock(List<String> blocks, List<Integer> heights, String html, int height) {
        if (html != null && !html.isEmpty()) {
            blocks.add(html);
            heights.add(height);
        }
    }

    /** 배너 아래 블록들 조립: 첫 블록 위 gap=firstGap, 이후 32. gap 은 wrapper div 의 margin-top 으로. */
    private String joinBlocks(List<String> blocks, int firstGap) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < blocks.size(); i++) {
            int mt = (i == 0) ? firstGap : SECTION_GAP;
            sb.append("<div style=\"margin-top:").append(mt).append("px\">").append(blocks.get(i)).append("</div>");
        }
        return sb.toString();
    }

    private int blocksHeight(List<Integer> heights, int firstGap) {
        if (heights.isEmpty()) {
            return 0;
        }
        int h = firstGap;
        for (int i = 0; i < heights.size(); i++) {
            h += heights.get(i);
            if (i > 0) {
                h += SECTION_GAP;
            }
        }
        return h;
    }

    /** 발견 섹션 = head + 단일 흰 박스(스타일 텍스트). */
    private String discoveredSection(StyledText body) {
        String bodyHtml = StyledTextHtmlRenderer.render(body);
        if (bodyHtml.isEmpty()) {
            return "";
        }
        return "<div class=\"r-section\">" + sectionHead(assets.sectionDiscoveredIcon(), "이런 면도 발견되었어요")
                + "<div class=\"r-box\"><div class=\"r-box-body\">" + bodyHtml + "</div></div></div>";
    }

    /** 한 마디 섹션 = head + 문장별 흰 박스(FE splitSegmentsBySentence 미러). */
    private String commentSection(StyledText body) {
        List<StyledTextHtmlRenderer.Sentence> sentences = StyledTextHtmlRenderer.renderSentences(body);
        if (sentences.isEmpty()) {
            return "";
        }
        // 한 마디 박스 = 테두리 없음·padding 8/16·박스 간격 16(Figma). 발견 박스(.r-box)와 구분되는 .r-comment-box.
        StringBuilder boxes = new StringBuilder();
        for (StyledTextHtmlRenderer.Sentence s : sentences) {
            boxes.append("<div class=\"r-comment-box\"><div class=\"r-box-body\">")
                    .append(s.html()).append("</div></div>");
        }
        return "<div class=\"r-section\">" + sectionHead(assets.sectionCommentIcon(), "나답의 한 마디")
                + boxes + "</div>";
    }

    private String sectionHead(Optional<String> icon, String head) {
        String iconHtml = icon.map(u -> "<img src=\"" + u + "\" alt=\"\"/>").orElse("");
        return "<div class=\"r-section-head\">" + iconHtml + PdfHtml.escape(head) + "</div>";
    }

    private String radar(MonthlyReportV2 m) {
        TypeEmotionStatsContent stats = m.getEmotionStats();
        List<EmotionStat> current = (stats == null) ? List.of() : stats.emotions();
        List<EmotionStat> previous = null;
        Integer prevMonth = null;
        boolean hasPrev = false;
        if (m.getEmotionComparison() != null && m.getEmotionComparison().previousEmotionStats() != null) {
            previous = m.getEmotionComparison().previousEmotionStats().emotions();
            prevMonth = m.getEmotionComparison().previousMonth();
            hasPrev = previous != null && previous.stream()
                    .anyMatch(s -> s != null && s.percent() != null && s.percent() > 0);
        }
        Optional<EmotionRadarChartRenderer.RadarChart> chart = radarRenderer.render(current, previous);
        if (chart.isEmpty()) {
            return "";
        }
        StringBuilder labels = new StringBuilder();
        for (EmotionRadarChartRenderer.RadarLabel l : chart.get().labels()) {
            labels.append("<div class=\"rl\" style=\"left:").append(l.x()).append("px;top:").append(l.y())
                    .append("px\"><div class=\"rl-name\">").append(PdfHtml.escape(l.name()))
                    .append("</div><div class=\"rl-pct\">").append(l.percent()).append("%</div></div>");
        }
        // 차트 아래 월 라벨(왼쪽 정렬): 지난 달(핑크) → 이번 달(브랜드) 순, 비교 없으면 이번 달만.
        int curMonth = m.getMonthStartDate().getMonthValue();
        StringBuilder months = new StringBuilder("<div class=\"radar-months\">");
        if (hasPrev && prevMonth != null) {
            months.append(monthChip("#f657e6", prevMonth));
        }
        months.append(monthChip("#5d57f6", curMonth)).append("</div>");

        // 레이더 박스 안 감정 요약(월 라벨 아래) = emotionSummaryContent(스타일드, 비교 인지 AI 생성). 마크 있으면 볼드+형광펜.
        String noteBody = StyledTextHtmlRenderer.render(
                m.getEmotionSummaryContent() == null ? null : m.getEmotionSummaryContent().styledText());
        String noteHtml = noteBody.isEmpty() ? "" : "<div class=\"radar-note\">" + noteBody + "</div>";

        return "<div class=\"radar-box\"><div class=\"radar-chart\"><img src=\"" + chart.get().imageDataUri()
                + "\" alt=\"\"/>" + labels + "</div>" + months + noteHtml + "</div>";
    }

    /** 레이더 박스 높이: 기본(박스+차트+월라벨) + (문구 있으면 gap + 줄*줄높이). */
    private int radarBoxHeight(String note) {
        int h = RADAR_BOX_BASE_H;
        if (note != null && !note.isBlank()) {
            h += RADAR_NOTE_GAP + textLines(note, bodyFont, WHITE_INNER) * REPORT_BODY_LH;
        }
        return h;
    }

    private String monthChip(String colorHex, int month) {
        return "<span class=\"m\"><span class=\"dot\" style=\"background:" + colorHex
                + "\"></span><span class=\"mlabel\">" + month + "월</span></span>";
    }

    private String banner(String kicker, String title) {
        // 배너 bg img 는 절대배치라 텍스트도 절대배치+z-index 로 위에. 위 모서리 라운드는 img border-radius(CSS)로 클리핑.
        String bg = assets.reportBanner()
                .map(u -> "<img class=\"bg\" src=\"" + u + "\" alt=\"\"/>").orElse("");
        // summary 미생성 리포트(초기 데이터)는 title 이 비어 — 따옴표만 남지 않게 제목 줄 생략.
        String titleHtml = (title == null || title.isBlank())
                ? "" : "<div class=\"r-title\">“" + PdfHtml.escape(title) + "”</div>";
        return "<div class=\"r-banner\">" + bg
                + "<div class=\"r-text\"><div class=\"r-kicker\">" + PdfHtml.escape(kicker) + "</div>"
                + titleHtml + "</div></div>";
    }

    /* ── 높이 추정(셀 배정용) ── */

    private static String plain(StyledText st) {
        return st == null ? "" : st.plainText();
    }

    /** 발견 섹션 높이(gap 제외): head + 흰 박스(오버헤드 + 줄*줄높이). 리포트 본문 한글이라 Regular 측정으로 충분. */
    private int discoveredHeight(String plain) {
        if (plain == null || plain.isBlank()) {
            return 0;
        }
        int lines = textLines(plain, bodyFont, WHITE_INNER);
        return SECTION_HEAD_H + BOX_OVERHEAD + lines * REPORT_BODY_LH;
    }

    /** 한 마디 섹션 높이(gap 제외): head + 문장별 박스들(각 오버헤드 + 줄*줄높이) + 박스 간 gap. */
    private int commentHeight(StyledText body) {
        List<StyledTextHtmlRenderer.Sentence> sentences = StyledTextHtmlRenderer.renderSentences(body);
        if (sentences.isEmpty()) {
            return 0;
        }
        int h = SECTION_HEAD_H;
        for (int i = 0; i < sentences.size(); i++) {
            int lines = textLines(sentences.get(i).plain(), bodyFont, WHITE_INNER);
            h += COMMENT_BOX_OVERHEAD + lines * REPORT_BODY_LH;
            if (i > 0) {
                h += COMMENT_BOX_GAP;
            }
        }
        return h;
    }

    /** 폭 widthPx 안에서 text 가 몇 줄로 접히는지 — 실제 Pretendard glyph advance 로 그리디 줄바꿈(렌더와 일치). */
    private int textLines(String text, Font font, int widthPx) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int lines = 1;
        double w = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                lines++;
                w = 0;
                continue;
            }
            double adv = advance(font, ch);
            if (w + adv > widthPx) {
                lines++;
                w = adv;
            } else {
                w += adv;
            }
        }
        return lines;
    }

    private double advance(Font font, char ch) {
        return advCache.computeIfAbsent(font, f -> new ConcurrentHashMap<>())
                .computeIfAbsent(ch, c -> (double) font.createGlyphVector(frc, String.valueOf(c)).getGlyphMetrics(0).getAdvance());
    }

    /* ── 문서 조립 ── */

    private String document(List<List<Positioned>> pages) {
        StringBuilder sb = new StringBuilder(8192);
        sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta charset=\"UTF-8\"/><style>")
                .append(css).append("</style></head><body>");
        int total = pages.size();
        for (int i = 0; i < total; i++) {
            // 두 열 사이 세로 점선 divider(오버샘플 PNG 컬럼을 다운스케일 표시).
            sb.append("<div class=\"page\"><img class=\"divider\" alt=\"\" width=\"1\" height=\"").append(COL_H)
                    .append("\" src=\"").append(assets.dottedColumn(COL_H)).append("\"/>");
            for (Positioned p : pages.get(i)) {
                sb.append("<img class=\"cardshadow\" alt=\"\" style=\"left:").append(p.x() - PdfShadowRenderer.BLUR)
                        .append("px;top:").append(p.y() - PdfShadowRenderer.BLUR + PdfShadowRenderer.OFFSET_Y)
                        .append("px;width:").append(p.w() + 2 * PdfShadowRenderer.BLUR)
                        .append("px;height:").append(p.h() + 2 * PdfShadowRenderer.BLUR)
                        .append("px;\" src=\"").append(shadowRenderer.assetUri(p.w(), p.h())).append("\"/>");
                sb.append("<div class=\"card\" style=\"left:").append(p.x()).append("px;top:").append(p.y())
                        .append("px;width:").append(p.w()).append("px;height:").append(p.h()).append("px;\">")
                        .append(p.html()).append("</div>");
            }
            sb.append(footer(i + 1)).append("</div>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private String footer(int pageNo) {
        String logo = assets.footerLogo().map(u -> "<img src=\"" + u + "\" alt=\"\"/>").orElse("");
        return "<div class=\"footer\"><span class=\"brand\">" + logo + "나에게 답하다</span>"
                + "<span class=\"pageno\">" + pageNo + "</span></div>";
    }

    /**
     * assemble 결과: XHTML + 답변 사진 인라인 애셋 맵(asset:photo-N 토큰 → JPEG 바이트).
     * 렌더러가 맵을 받아 asset: 프로토콜 스트림 팩토리로 서빙한다(반복 baked 에셋과 동일 경로).
     */
    public record AssembledDocument(String xhtml, Map<String, byte[]> inlineAssets) {
    }

    private record Block(int estH, String html) {
    }

    /** 정렬용 래퍼: date+tier 로 시간순 인터리브(답변→그 주 주간→…→월간). */
    private record Sortable(LocalDate date, int tier, Block block) {
    }

    private record Positioned(int x, int y, int w, int h, String html) {
    }
}
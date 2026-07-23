package com.devkor.ifive.nadab.global.core.pdf;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PDF 렌더 정적 애셋(폰트·이미지)을 클래스패스에서 최초 1회 로드·캐시.
 * 반복 에셋(배너·divider·아이콘·로고·섹션)은 asset: 토큰으로 참조한다 — 게터는 asset:키 문자열을 주고,
 * 바이트는 assetBytes 로 렌더러 스트림 팩토리가 서빙해 동일 에셋을 1회만 디코드한다. 폰트는 필수(없으면 startup 실패).
 */
@Slf4j
@Component
public class PdfAssetLoader {

    /** 렌더러 폰트 등록과 CSS가 공유하는 논리 폰트명. */
    public static final String FONT_FAMILY = "Pretendard";

    private static final String FONT_REGULAR_PATH = "fonts/Pretendard-Regular.ttf";
    private static final String FONT_BOLD_PATH = "fonts/Pretendard-Bold.ttf";

    private static final String IMG_DIR = "images/pdf/";
    private static final String ICON_DIR = "images/pdf/icon/";

    /** 관심사 아이콘 파일 키(InterestCode.name()과 동일 — 레이어 분리 위해 enum 직접 참조 안 함). */
    private static final List<String> INTEREST_CODES =
            List.of("PREFERENCE", "EMOTION", "ROUTINE", "RELATIONSHIP", "LOVE", "VALUES");

    /* HIGHLIGHT 형광펜 바: 상단 흰색·하단 연보라 불투명 세로 PNG를 background 하단정렬·가로반복(gradient 미지원 우회).
       상수는 14px/line-height 1.8 기준 실측 — 폰트·크기 바뀌면 재측정. */
    private static final int HL_BAR_HEIGHT = 40;      // 이미지 높이(인라인 박스보다 큼)
    private static final int HL_BAR_FILL = 9;         // 하단 색칠 높이 → 렌더 시 ≈7px = 글자 아래 절반
    private static final int HL_BAR_COLOR = 0xE7E6FE; // brand @15% over #fff (불투명)
    private static final int HL_BAR_TOP = 0xFFFFFF;   // 흰 박스 배경색(상단)

    /** weight=400(Regular)/700(Bold), style은 둘 다 normal. */
    public record FontFace(int weight, byte[] data) {
    }

    private List<FontFace> pretendardFaces;
    private Optional<byte[]> reportBannerBytes;
    private Optional<byte[]> footerLogoBytes;
    private Optional<byte[]> sectionDiscoveredIconBytes;
    private Optional<byte[]> sectionCommentIconBytes;
    private Map<String, byte[]> interestIconBytes;
    /** HIGHLIGHT 바는 CSS에 1회만 박혀(반복 아님) data URI. */
    private String highlightBar;
    /** 세로 점선 divider PNG 바이트(표시 높이별 캐시). asset:divider-{h} 로 서빙. */
    private final Map<Integer, byte[]> dividerBytes = new ConcurrentHashMap<>();

    @PostConstruct
    void load() {
        pretendardFaces = List.of(
                new FontFace(400, readRequired(FONT_REGULAR_PATH)),
                new FontFace(700, readRequired(FONT_BOLD_PATH))
        );

        reportBannerBytes = readOptional(IMG_DIR + "report-banner.png");
        footerLogoBytes = readOptional(IMG_DIR + "logo.png");
        sectionDiscoveredIconBytes = readOptional(ICON_DIR + "section-discovered.png");
        sectionCommentIconBytes = readOptional(ICON_DIR + "section-comment.png");

        Map<String, byte[]> icons = new LinkedHashMap<>();
        for (String code : INTEREST_CODES) {
            readOptional(ICON_DIR + "interest-" + code.toLowerCase() + ".png")
                    .ifPresent(bytes -> icons.put(code, bytes));
        }
        interestIconBytes = Map.copyOf(icons);

        highlightBar = makeHighlightBar();

        log.debug("PDF 애셋 로드 — 폰트 {}종, 배너 {}, 로고 {}, 관심사 아이콘 {}종",
                pretendardFaces.size(),
                reportBannerBytes.isPresent() ? "O" : "폴백",
                footerLogoBytes.isPresent() ? "O" : "폴백",
                interestIconBytes.size());
    }

    public List<FontFace> pretendardFaces() {
        return pretendardFaces;
    }

    /* ── 반복 에셋: asset: 토큰 반환(있을 때만 — 없으면 empty 로 폴백 유지). 바이트는 assetBytes()로 팩토리가 서빙 ── */

    public Optional<String> reportBanner() {
        return reportBannerBytes.map(b -> "asset:banner");
    }

    public Optional<String> footerLogo() {
        return footerLogoBytes.map(b -> "asset:logo");
    }

    public Optional<String> sectionDiscoveredIcon() {
        return sectionDiscoveredIconBytes.map(b -> "asset:section-discovered");
    }

    public Optional<String> sectionCommentIcon() {
        return sectionCommentIconBytes.map(b -> "asset:section-comment");
    }

    /** code = InterestCode.name(). 아이콘 파일 없으면 empty. */
    public Optional<String> interestIcon(String code) {
        return interestIconBytes.containsKey(code) ? Optional.of("asset:icon-" + code) : Optional.empty();
    }

    /** 두 열 사이 세로 점선 divider. 표시 높이별 1회 생성·캐시. asset:divider-{h} 토큰 반환. */
    public String dottedColumn(int displayHeightPx) {
        dividerBytes.computeIfAbsent(displayHeightPx, PdfAssetLoader::makeDottedColumnBytes);
        return "asset:divider-" + displayHeightPx;
    }

    /** HIGHLIGHT 형광펜 바 data URI(CSS __HL_BAR__ 치환용 — CSS에 1회 참조라 인라인). */
    public String highlightBar() {
        return highlightBar;
    }

    /**
     * asset: 스트림 팩토리(렌더러)용 바이트 조회. key = 토큰에서 asset: 뗀 부분.
     * 예: banner · logo · section-discovered · section-comment · icon-{CODE} · divider-{h}. (shadow-* 는 PdfShadowRenderer 담당.)
     */
    public Optional<byte[]> assetBytes(String key) {
        if (key.equals("banner")) {
            return reportBannerBytes;
        }
        if (key.equals("logo")) {
            return footerLogoBytes;
        }
        if (key.equals("section-discovered")) {
            return sectionDiscoveredIconBytes;
        }
        if (key.equals("section-comment")) {
            return sectionCommentIconBytes;
        }
        if (key.startsWith("icon-")) {
            return Optional.ofNullable(interestIconBytes.get(key.substring("icon-".length())));
        }
        if (key.startsWith("divider-")) {
            int h = Integer.parseInt(key.substring("divider-".length()));
            return Optional.of(dividerBytes.computeIfAbsent(h, PdfAssetLoader::makeDottedColumnBytes));
        }
        return Optional.empty();
    }

    private static String makeHighlightBar() {
        int w = 8;
        int h = HL_BAR_HEIGHT;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) {
            int c = (y >= h - HL_BAR_FILL) ? HL_BAR_COLOR : HL_BAR_TOP;
            for (int x = 0; x < w; x++) {
                img.setRGB(x, y, c);
            }
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("HIGHLIGHT 형광펜 바 생성 실패", e);
        }
    }

    private static final int DOTTED_OVERSAMPLE = 4;    // 가로 오버샘플(표시 1px)
    private static final int DOTTED_V_OVERSAMPLE = 8;  // 세로 오버샘플

    /** 세로 점선 divider PNG 바이트 생성(표시 높이별). */
    private static byte[] makeDottedColumnBytes(int displayHeightPx) {
        int w = DOTTED_OVERSAMPLE;
        int h = displayHeightPx * DOTTED_V_OVERSAMPLE;
        int dot = 2 * DOTTED_V_OVERSAMPLE;
        int period = 4 * DOTTED_V_OVERSAMPLE;
        int color = 0xFFC8C8C8;                             // #C8C8C8 불투명(점)
        int gapColor = color & 0x00FFFFFF;                  // 간격 = 점색 RGB + 알파0 (다크 프린지 제거)
        BufferedImage col = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            int c = (y % period < dot) ? color : gapColor;
            for (int x = 0; x < w; x++) {
                col.setRGB(x, y, c);
            }
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(col, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("divider 점선 컬럼 생성 실패", e);
        }
    }

    private byte[] readRequired(String path) {
        return readOptional(path).orElseThrow(() -> {
            String message = "필수 PDF 애셋 없음: %s (Pretendard 정적 TTF를 src/main/resources/%s 에 배치)"
                    .formatted(path, path);
            log.error(message);
            return new IllegalStateException(message);
        });
    }

    private Optional<byte[]> readOptional(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            return Optional.empty();
        }
        try {
            return Optional.of(resource.getContentAsByteArray());
        } catch (IOException e) {
            log.error("PDF 애셋 읽기 실패: {}", path, e);
            throw new IllegalStateException("PDF 애셋 읽기 실패: " + path, e);
        }
    }
}
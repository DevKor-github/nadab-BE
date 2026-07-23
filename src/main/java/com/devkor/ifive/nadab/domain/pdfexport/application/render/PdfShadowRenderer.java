package com.devkor.ifive.nadab.domain.pdfexport.application.render;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 카드 소프트 그림자 PNG(openhtmltopdf box-shadow 미지원 우회). 크기별 캐시.
 * 목업 토큰(offset y4·blur 12·#00000033=검정 20%) 재현 = 20% 라운드렉트를 박스블러로 퍼뜨림.
 */
@Component
class PdfShadowRenderer {

    /** 카드 사방 여유(=스프라이트 패딩) · 아래 오프셋 · 라운드 · 블러 반경/횟수 · 피크 알파(0x33=20%). */
    static final int BLUR = 16;
    static final int OFFSET_Y = 4;
    private static final int RADIUS = 16;
    private static final int BLUR_RADIUS = 6;
    private static final int BLUR_PASSES = 3;
    private static final int PEAK_ALPHA = 51;

    private final Map<Long, byte[]> cache = new ConcurrentHashMap<>();

    /** asset: 토큰 반환(어셈블러가 XHTML 에 박음). 실제 PNG 바이트는 bytes() 로 렌더러 스트림 팩토리가 서빙. */
    String assetUri(int w, int h) {
        return "asset:shadow-" + w + "x" + h;
    }

    /** 크기별 그림자 PNG 바이트(표시 크기 캐시). */
    byte[] bytes(int w, int h) {
        return cache.computeIfAbsent((((long) w) << 32) | (h & 0xffffffffL), k -> render(w, h));
    }

    private byte[] render(int w, int h) {
        int fullW = w + 2 * BLUR;
        int fullH = h + 2 * BLUR;

        BufferedImage base = new BufferedImage(fullW, fullH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = base.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0, 0, 0, PEAK_ALPHA));
        g.fillRoundRect(BLUR, BLUR, w, h, RADIUS, RADIUS);
        g.dispose();

        int[] pixels = ((DataBufferInt) base.getRaster().getDataBuffer()).getData();
        int[] alpha = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            alpha[i] = pixels[i] >>> 24;
        }
        for (int p = 0; p < BLUR_PASSES; p++) {
            alpha = boxBlur(alpha, fullW, fullH, BLUR_RADIUS);
        }

        BufferedImage out = new BufferedImage(fullW, fullH, BufferedImage.TYPE_INT_ARGB);
        int[] outPixels = ((DataBufferInt) out.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < outPixels.length; i++) {
            outPixels[i] = alpha[i] << 24;
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(out, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("그림자 PNG 인코딩 실패", e);
        }
    }

    /** 분리형 박스블러 1패스(수평→수직). 3패스면 가우시안에 근사. */
    private int[] boxBlur(int[] src, int w, int h, int r) {
        int[] tmp = new int[src.length];
        for (int y = 0; y < h; y++) {
            int row = y * w;
            for (int x = 0; x < w; x++) {
                int sum = 0;
                int count = 0;
                for (int k = -r; k <= r; k++) {
                    int xx = x + k;
                    if (xx >= 0 && xx < w) {
                        sum += src[row + xx];
                        count++;
                    }
                }
                tmp[row + x] = sum / count;
            }
        }
        int[] dst = new int[src.length];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int sum = 0;
                int count = 0;
                for (int k = -r; k <= r; k++) {
                    int yy = y + k;
                    if (yy >= 0 && yy < h) {
                        sum += tmp[yy * w + x];
                        count++;
                    }
                }
                dst[y * w + x] = sum / count;
            }
        }
        return dst;
    }
}
package com.devkor.ifive.nadab.domain.pdfexport.application.render;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 답변 사진 렌더 유틸(정중앙 정사각 크롭 + 리샘플 → JPEG 바이트). openhtmltopdf가 object-fit/aspect-ratio 미지원이라 여기서 굽는다.
 * 프로덕션 Listener·프리뷰 하네스 공유 단일 지점. webp는 twelvemonkeys 리더로 ImageIO 가 바로 디코드(사전 변환 불필요).
 * 결과 바이트는 어셈블러가 asset: 토큰으로 참조하고 렌더러 스트림 팩토리가 서빙한다 — XHTML 에 base64 로 인라인되지 않는다.
 */
public final class PdfImage {

    private PdfImage() {
    }

    /**
     * 원본 이미지 바이트 → 정중앙 정사각 크롭 → size×size 리샘플 → JPEG 바이트.
     * source = 디코드 가능한 이미지 바이트(png/jpg/webp — webp는 twelvemonkeys 리더로 ImageIO 직접 디코드), size = 출력 한 변 px(오버샘플 배수).
     */
    public static byte[] coverSquareJpegBytes(byte[] source, int size) {
        try {
            BufferedImage src = ImageIO.read(new ByteArrayInputStream(source));
            if (src == null) {
                throw new IOException("이미지 디코드 실패(지원하지 않는 포맷)");
            }
            return encodeJpeg(coverSquare(src, size));
        } catch (IOException e) {
            throw new IllegalStateException("답변 사진 렌더 실패", e);
        }
    }

    /** 원본의 짧은 변 기준 정중앙 정사각 영역을 잘라 size×size 로 그린다(한 번의 drawImage 로 크롭+스케일). */
    private static BufferedImage coverSquare(BufferedImage src, int size) {
        int w = src.getWidth();
        int h = src.getHeight();
        int crop = Math.min(w, h);
        int sx = (w - crop) / 2;
        int sy = (h - crop) / 2;

        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        try {
            // TYPE_INT_RGB 는 알파가 없어 투명 영역이 기본 검정으로 평탄화된다. 흰 답변박스 위에 얹히므로
            // 흰색을 먼저 깔아 투명 PNG 답변 사진도 카드와 자연스럽게 이어지게 한다(JPEG 유지 = 용량 영향 0).
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

    private static byte[] encodeJpeg(BufferedImage img) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.82f);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(img, null, null), param);
        } finally {
            writer.dispose();
        }
        return baos.toByteArray();
    }
}
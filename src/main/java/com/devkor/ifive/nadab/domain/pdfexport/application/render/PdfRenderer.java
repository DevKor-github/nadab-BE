package com.devkor.ifive.nadab.domain.pdfexport.application.render;

import com.devkor.ifive.nadab.global.core.pdf.PdfAssetLoader;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * XHTML → PDF (openhtmltopdf/PDFBox). Pretendard TTF를 임베드(subset)한다.
 * 이미지는 전부 data URI 라 외부 리소스 해석이 없어 baseUri 불필요.
 */
@Component
@RequiredArgsConstructor
public class PdfRenderer {

    private final PdfAssetLoader assets;

    public byte[] render(String xhtml) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            for (PdfAssetLoader.FontFace face : assets.pretendardFaces()) {
                byte[] data = face.data();
                builder.useFont(() -> new ByteArrayInputStream(data),
                        PdfAssetLoader.FONT_FAMILY, face.weight(), FontStyle.NORMAL, true);
            }
            builder.withHtmlContent(xhtml, "");
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("PDF 렌더 실패", e);
        }
    }
}
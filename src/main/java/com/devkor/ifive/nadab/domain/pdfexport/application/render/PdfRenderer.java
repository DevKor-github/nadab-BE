package com.devkor.ifive.nadab.domain.pdfexport.application.render;

import com.devkor.ifive.nadab.global.core.pdf.PdfAssetLoader;
import com.openhtmltopdf.extend.FSStream;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * XHTML → PDF (openhtmltopdf/PDFBox). Pretendard TTF를 임베드(subset)한다.
 * 레이더·하이라이트는 data URI(1회/고유)지만, 반복 baked 에셋(배너·divider·아이콘·로고·섹션·그림자)과 답변 사진은
 * asset: 프로토콜로 서빙한다 — 어셈블러가 asset:키 토큰만 박고, 여기 스트림 팩토리가 바이트를 준다.
 * 결과 PDF와 PDFBox 문서모델은 힙 밖 임시파일에 둔다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PdfRenderer {

    private final PdfAssetLoader assets;
    private final PdfShadowRenderer shadows;

    /**
     * inlineAssets = 답변 사진 애셋 맵(asset:photo-N → JPEG 바이트, 어셈블러가 렌더별로 채움). 사진 없으면 빈 맵.
     * 결과는 임시파일 Path — 호출부가 업로드 후 삭제 책임을 진다. 렌더 실패 시 여기서 임시파일을 지우고 던진다.
     */
    public Path render(String xhtml, Map<String, byte[]> inlineAssets) {
        Path pdfFile;
        try {
            pdfFile = Files.createTempFile("pdf-export-", ".pdf");
        } catch (IOException e) {
            throw new IllegalStateException("PDF 임시파일 생성 실패", e);
        }
        boolean rendered = false;
        // PDFBox 문서모델을 힙 밖 임시파일에 둔다(createTempFileOnlyStreamCache). usePDDocument 사용 시 close 는 우리 책임.
        try (OutputStream os = Files.newOutputStream(pdfFile);
             PDDocument pdDocument = new PDDocument(IOUtils.createTempFileOnlyStreamCache())) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            for (PdfAssetLoader.FontFace face : assets.pretendardFaces()) {
                byte[] data = face.data();
                builder.useFont(() -> new ByteArrayInputStream(data),
                        PdfAssetLoader.FONT_FAMILY, face.weight(), FontStyle.NORMAL, true);
            }
            // baked 에셋·답변 사진을 asset: 프로토콜로 서빙. extractProtocol=첫 ':' 앞 → "asset".
            builder.useProtocolsStreamImplementation(uri -> openAsset(uri, inlineAssets), "asset");
            builder.withHtmlContent(xhtml, "");
            builder.toStream(os);
            builder.usePDDocument(pdDocument);
            builder.run();
            rendered = true;
            return pdfFile;
        } catch (IOException e) {
            throw new IllegalStateException("PDF 렌더 실패", e);
        } finally {
            if (!rendered) {
                deleteQuietly(pdfFile);
            }
        }
    }

    /** 임시파일 삭제(실패해도 삼킨다). 렌더 실패 정리·크래시 잔여 대비. */
    private void deleteQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("[PDF_EXPORT] PDF 임시파일 삭제 실패: {}", file, e);
        }
    }

    /** asset:KEY URI → 바이트 스트림(FSStreamFactory.getUrl). */
    private FSStream openAsset(String uri, Map<String, byte[]> inlineAssets) {
        byte[] bytes = resolveAsset(uri, inlineAssets);
        return new FSStream() {
            @Override
            public InputStream getStream() {
                return new ByteArrayInputStream(bytes);
            }

            @Override
            public Reader getReader() {
                return new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8);
            }
        };
    }

    /**
     * asset:KEY 해석. photo-N 은 렌더별 사진 맵, shadow-{w}x{h} 는 PdfShadowRenderer,
     * 나머지(banner·logo·icon-*·divider-*·section-*)는 PdfAssetLoader.
     */
    private byte[] resolveAsset(String uri, Map<String, byte[]> inlineAssets) {
        String key = uri.substring(uri.indexOf(':') + 1); // "asset:" 뒤
        if (key.startsWith("photo-")) {
            byte[] bytes = inlineAssets.get(key);
            if (bytes == null) {
                throw new IllegalStateException("알 수 없는 사진 에셋 URI: " + uri);
            }
            return bytes;
        }
        if (key.startsWith("shadow-")) {
            String[] wh = key.substring("shadow-".length()).split("x");
            return shadows.bytes(Integer.parseInt(wh[0]), Integer.parseInt(wh[1]));
        }
        return assets.assetBytes(key)
                .orElseThrow(() -> new IllegalStateException("알 수 없는 PDF 에셋 URI: " + uri));
    }
}
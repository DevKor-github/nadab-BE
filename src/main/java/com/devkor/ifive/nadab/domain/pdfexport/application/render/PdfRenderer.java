package com.devkor.ifive.nadab.domain.pdfexport.application.render;

import com.devkor.ifive.nadab.global.core.pdf.PdfAssetLoader;
import com.openhtmltopdf.extend.FSStream;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * XHTML → PDF (openhtmltopdf/PDFBox). Pretendard TTF를 임베드(subset)한다.
 * 레이더·하이라이트는 data URI(1회/고유)지만, 반복 baked 에셋(배너·divider·아이콘·로고·섹션·그림자)과 답변 사진은
 * asset: 프로토콜로 서빙한다 — 어셈블러가 asset:키 토큰만 박고, 여기 스트림 팩토리가 바이트를 준다.
 * 반복 에셋은 URI 캐시로 1회만 디코드되고, 답변 사진은 고유라 캐시는 없지만 XHTML 에 base64 로 인라인되지 않는다.
 */
@Component
@RequiredArgsConstructor
public class PdfRenderer {

    private final PdfAssetLoader assets;
    private final PdfShadowRenderer shadows;

    /** inlineAssets = 답변 사진 애셋 맵(asset:photo-N → JPEG 바이트, 어셈블러가 렌더별로 채움). 사진 없으면 빈 맵. */
    public byte[] render(String xhtml, Map<String, byte[]> inlineAssets) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
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
            builder.run();
            return os.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("PDF 렌더 실패", e);
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
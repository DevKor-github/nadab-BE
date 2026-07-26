package com.devkor.ifive.nadab.domain.pdfexport.application.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * 부팅 시 임시파일 청소 검증. 지우는 경계(이름·나이)를 고정한다.
 */
class PdfExportTempFileCleanerTest {

    @TempDir
    Path tempDir;

    @Test
    void 오래된_렌더_임시파일을_지운다() throws IOException {
        Path orphan = renderTempFile("pdf-export-1234.pdf", Duration.ofHours(2));

        clean();

        assertThat(orphan).doesNotExist();
    }

    @Test
    void 최근_파일은_남긴다() throws IOException {
        // 배포가 겹치면 이전 프로세스가 아직 렌더 중일 수 있다. 이걸 지우면 멀쩡한 작업을 깨뜨린다.
        Path inFlight = renderTempFile("pdf-export-5678.pdf", Duration.ofMinutes(5));

        clean();

        assertThat(inFlight).exists();
    }

    @Test
    void 우리가_만들지_않은_파일은_건드리지_않는다() throws IOException {
        // 같은 임시 디렉터리를 PDFBox scratch·다른 라이브러리·OS가 함께 쓴다.
        Path foreign = renderTempFile("PDFBox-scratch-1.tmp", Duration.ofDays(1));
        Path notPdf = renderTempFile("pdf-export-9999.txt", Duration.ofDays(1));

        clean();

        assertThat(foreign).exists();
        assertThat(notPdf).exists();
    }

    @Test
    void 디렉터리가_없어도_부팅을_막지_않는다() {
        PdfExportTempFileCleaner cleaner =
                new PdfExportTempFileCleaner(tempDir.resolve("없는-디렉터리").toString());

        assertThatCode(cleaner::cleanOrphans).doesNotThrowAnyException();
    }

    private void clean() {
        new PdfExportTempFileCleaner(tempDir.toString()).cleanOrphans();
    }

    private Path renderTempFile(String name, Duration age) throws IOException {
        Path file = Files.createFile(tempDir.resolve(name));
        Files.setLastModifiedTime(file, FileTime.from(Instant.now().minus(age)));
        return file;
    }
}
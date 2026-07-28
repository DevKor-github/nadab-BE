package com.devkor.ifive.nadab.domain.pdfexport.application.helper;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * 이전 실행이 강제 종료로 남긴 렌더 임시파일을 부팅 시 한 번 지운다.
 * 정상·예외 경로는 렌더러가 스스로 정리하므로 여기 걸리는 건 그 몫뿐이다.
 */
@Component
@Slf4j
public class PdfExportTempFileCleaner {

    /** PdfRenderer 가 만드는 결과 임시파일 이름. 우리가 만든 것만 지운다. */
    private static final String RENDER_TEMP_GLOB = "pdf-export-*.pdf";

    /** 이보다 오래된 것만 지운다. 다른 프로세스가 렌더 중인 파일을 뺏지 않기 위한 여유. */
    private static final Duration MIN_AGE = Duration.ofHours(1);

    private final Path tempDir;

    public PdfExportTempFileCleaner(@Value("${java.io.tmpdir}") String tempDir) {
        this.tempDir = Path.of(tempDir);
    }

    /** 어떤 이유로 실패해도 부팅을 막지 않는다. */
    @PostConstruct
    public void cleanOrphans() {
        Instant deleteBefore = Instant.now().minus(MIN_AGE);
        int deleted = 0;
        try (DirectoryStream<Path> files = Files.newDirectoryStream(tempDir, RENDER_TEMP_GLOB)) {
            for (Path file : files) {
                if (deleteIfOlderThan(file, deleteBefore)) {
                    deleted++;
                }
            }
        } catch (Exception e) {
            log.warn("[PDF_EXPORT] 임시파일 청소를 건너뜀: {}", tempDir, e);
            return;
        }
        if (deleted > 0) {
            log.info("[PDF_EXPORT] 이전 실행이 남긴 임시파일 정리: {}건", deleted);
        }
    }

    private boolean deleteIfOlderThan(Path file, Instant deleteBefore) {
        try {
            if (Files.getLastModifiedTime(file).toInstant().isAfter(deleteBefore)) {
                return false;
            }
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("[PDF_EXPORT] 임시파일 삭제 실패: {}", file, e);
            return false;
        }
    }
}
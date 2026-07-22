package com.devkor.ifive.nadab.domain.pdfexport.api;

import com.devkor.ifive.nadab.domain.pdfexport.api.dto.request.PdfExportStartRequest;
import com.devkor.ifive.nadab.domain.pdfexport.api.dto.response.PdfExportArchiveItemResponse;
import com.devkor.ifive.nadab.domain.pdfexport.api.dto.response.PdfExportDownloadResponse;
import com.devkor.ifive.nadab.domain.pdfexport.api.dto.response.PdfExportPreviewResponse;
import com.devkor.ifive.nadab.domain.pdfexport.api.dto.response.PdfExportStartResponse;
import com.devkor.ifive.nadab.domain.pdfexport.api.dto.response.PdfExportStatusResponse;
import com.devkor.ifive.nadab.domain.pdfexport.application.PdfExportQueryService;
import com.devkor.ifive.nadab.domain.pdfexport.application.PdfExportService;
import com.devkor.ifive.nadab.global.core.response.ApiResponseDto;
import com.devkor.ifive.nadab.global.core.response.ApiResponseEntity;
import com.devkor.ifive.nadab.global.security.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "PDF 내보내기 API", description = "사용자 기록(답변/리포트) PDF 내보내기 관련 API")
@RestController
@RequestMapping("${api_prefix}/pdf-exports")
@RequiredArgsConstructor
public class PdfExportController {

    private final PdfExportService pdfExportService;
    private final PdfExportQueryService pdfExportQueryService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "PDF 내보내기 생성 시작",
            description = """
                    선택한 유형(리포트만/답변만/리포트+답변)과 기간(종료일 기준 최대 1년)으로 PDF 생성을 시작합니다. 호출 즉시 크리스탈이 차감됩니다. </br>
                    생성은 비동기라 이 API는 곧바로 jobId만 반환합니다. 완료 확인은 둘 중 하나로: </br>
                    (1) 생성 화면에 머물러 완료를 바로 반영하려면 GET /pdf-exports/{jobId}를 일정 시간 간격으로 폴링, </br>
                    (2) 생성 화면을 벗어나는 흐름이면 나중에 아카이브(GET /pdf-exports)에서 해당 작업이 COMPLETED로 뜨는지 확인(완료 시 FCM 푸시로 전송 후 사용자는 아카이브에서 확인). </br>
                    어느 쪽이든 COMPLETED가 되면 POST /pdf-exports/{jobId}/download-url 로 다운로드 URL을 발급받아 받으면 됩니다. </br>
                    응답의 balanceAfter는 차감 후 남은 크리스탈 잔액입니다(지갑 UI 갱신용). </br>
                    멱등 재사용: 같은 유형·기간의 작업이 아직 생성 중(PENDING/IN_PROGRESS)일 때 다시 호출하면(응답을 못 받아 재시도하거나 버튼 더블탭 등) 재과금 없이 그 작업을 그대로 돌려주며 balanceAfter=null 입니다(이중 과금 없음, 지갑 추가 차감 표시 금지). 완료(COMPLETED)된 작업은 재사용하지 않으므로 같은 기간 재요청은 새 작업으로 재과금됩니다(= 재생성). </br>
                    생성 실패는 이 응답이 아니라 폴링(GET /pdf-exports/{jobId})에서 status=FAILED(errorCode 포함)로만 확인됩니다(아카이브에는 FAILED 미노출). 차감된 크리스탈은 자동 환불되므로, 잔액을 표시 중이면 다시 조회해 갱신하세요. </br>
                    내보낼 답변/리포트가 하나도 없으면 PDF_EXPORT_NO_DATA로 거부됩니다 — 미리보기(GET /pdf-exports/preview)로 사전 확인을 권장합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "PDF 내보내기 시작 성공 (또는 기존 작업 재사용)",
                            content = @Content(schema = @Schema(implementation = PdfExportStartResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = """
                                    - ErrorCode: PDF_EXPORT_INVALID_PERIOD - 기간이 올바르지 않음(시작일 > 종료일, 종료일이 미래, 또는 1년 초과)
                                    - ErrorCode: PDF_EXPORT_NO_DATA - 해당 기간에 내보낼 답변/리포트가 없음
                                    - ErrorCode: WALLET_INSUFFICIENT_BALANCE - 크리스탈 잔액 부족
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(
                            responseCode = "404",
                            description = """
                                    - ErrorCode: USER_NOT_FOUND - 사용자를 찾을 수 없음
                                    - ErrorCode: WALLET_NOT_FOUND - 지갑을 찾을 수 없음
                                    """,
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<PdfExportStartResponse>> startPdfExport(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PdfExportStartRequest request
    ) {
        PdfExportStartResponse response = pdfExportService.start(principal.getId(), request);
        return ApiResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "PDF 내보내기 아카이브(이력) 목록",
            description = """
                    내 PDF 내보내기 작업을 최신순(생성순 DESC)으로 조회합니다. 대상은 생성중(PENDING/IN_PROGRESS)과 완료(COMPLETED)이며, 실패(FAILED)는 자동 환불된 상태라 목록에 포함되지 않습니다. </br>
                    아카이브 화면에서 이력을 보거나, 앱을 다시 켰을 때 진행 중이던 생성 작업(PENDING/IN_PROGRESS)을 이 목록에서 다시 확인하는 데 사용합니다. </br>
                    각 항목은 유형(type)·기간(startDate~endDate)·상태(status)를 담습니다. status가 PENDING/IN_PROGRESS면 아직 생성 중이므로 GET /pdf-exports/{jobId} 폴링으로 이어가면 됩니다. </br>
                    완료(COMPLETED) 항목은 expiresAt(다운로드 보관 만료 = 완료 + 7일)와 expired(만료 여부)를 포함합니다. expired=false면 다운로드 URL을 발급(POST /pdf-exports/{jobId}/download-url)해 받고, expired=true면 보관 기간이 지나 다운로드가 불가하므로 재생성이 필요합니다. </br>
                    같은 유형·기간으로 재생성하면 이전 완료 항목은 삭제되어 목록에는 가장 최근 것만 남습니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "아카이브 목록 조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = PdfExportArchiveItemResponse.class)), mediaType = "application/json")
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<List<PdfExportArchiveItemResponse>>> getPdfExportArchive(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<PdfExportArchiveItemResponse> response = pdfExportQueryService.getArchive(principal.getId());
        return ApiResponseEntity.ok(response);
    }

    @GetMapping("/preview")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "PDF 내보내기 미리보기(포함 개수)",
            description = """
                    해당 기간에 포함될 답변·주간 리포트·월간 리포트 개수를 돌려줍니다. 생성/차감 전 확인 팝업에서 "무엇이 몇 개 포함되는지" 보여주는 용도의 순수 조회입니다. </br>
                    유형과 무관하게 3종 개수를 모두 내려주므로, 선택한 유형에 맞는 값만 표시하면 됩니다(예: 답변만 선택 시 answerCount). </br>
                    세 개수가 모두 0이면 생성해도 빈 PDF라 생성 API가 PDF_EXPORT_NO_DATA로 거부합니다. 이 경우 생성 버튼을 비활성화하거나 기획에 맞게 팝업을 띄우면 됩니다. </br>
                    기간 규칙은 생성 API와 동일합니다(시작일 ≤ 종료일, 종료일 ≤ 오늘, 최대 1년) — 잘못된 기간이면 PDF_EXPORT_INVALID_PERIOD.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "미리보기 개수 조회 성공",
                            content = @Content(schema = @Schema(implementation = PdfExportPreviewResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "- ErrorCode: PDF_EXPORT_INVALID_PERIOD - 기간이 올바르지 않음(시작일 > 종료일, 종료일이 미래, 또는 1년 초과)",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    public ResponseEntity<ApiResponseDto<PdfExportPreviewResponse>> getPdfExportPreview(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        PdfExportPreviewResponse response = pdfExportService.preview(principal.getId(), startDate, endDate);
        return ApiResponseEntity.ok(response);
    }

    @GetMapping("/{jobId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "PDF 내보내기 상태 조회 (폴링)",
            description = """
                    jobId로 특정 작업 하나의 진행 상태를 조회합니다. 생성 시작(POST /pdf-exports) 직후 생성/로딩 화면에 머무는 동안, 또는 아카이브에서 진행 중인 항목 하나를 열어 볼 때, COMPLETED나 FAILED가 될 때까지 이 API를 주기적으로 폴링합니다. </br>
                    (아카이브 목록 화면에서 여러 진행 중 항목의 상태를 갱신할 때는 개별 폴링보다 목록 API(GET /pdf-exports)를 다시 호출하는 편이 낫습니다.) </br>
                    status 값: </br>
                    - PENDING: 생성 대기 중 </br>
                    - IN_PROGRESS: 생성 진행 중 </br>
                    - COMPLETED: 생성 완료. expiresAt(다운로드 보관 만료 = 완료 + 7일)와 expired(만료 여부)가 포함됩니다. expired=true면 보관 기간이 지나 다운로드가 불가하므로 재생성이 필요합니다. </br>
                    - FAILED: 생성 실패(errorCode 포함). 차감된 크리스탈은 자동 환불되므로, 잔액을 표시 중이면 다시 조회해 갱신하세요. </br>
                    다운로드 URL은 이 응답에 포함되지 않습니다. COMPLETED가 된 뒤 POST /pdf-exports/{jobId}/download-url 로 발급받으세요(발급 빈도 제한이 폴링에 영향을 주지 않도록 분리되어 있습니다).
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "상태 조회 성공",
                            content = @Content(schema = @Schema(implementation = PdfExportStatusResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(
                            responseCode = "403",
                            description = "- ErrorCode: PDF_EXPORT_ACCESS_FORBIDDEN - 본인 작업이 아님",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "- ErrorCode: PDF_EXPORT_JOB_NOT_FOUND - 작업을 찾을 수 없음",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<PdfExportStatusResponse>> getPdfExportStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long jobId
    ) {
        PdfExportStatusResponse response = pdfExportQueryService.getStatus(principal.getId(), jobId);
        return ApiResponseEntity.ok(response);
    }

    @PostMapping("/{jobId}/download-url")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "PDF 다운로드 URL 발급",
            description = """
                    완료된 PDF의 다운로드 URL(CloudFront 서명 URL)을 발급합니다. 폴링과 분리된 발급 전용 API로, 상태가 COMPLETED가 된 뒤(또는 아카이브 항목의 다운로드 버튼에서) 호출합니다. </br>
                    downloadUrl은 약 3분간 유효합니다. 3분은 다운로드를 '시작'할 수 있는 창이며, 한 번 시작된 전송은 3분을 넘겨도 끝까지 받아집니다. 만료 후에는 이 API를 다시 호출해 새 URL을 발급받으면 됩니다(같은 파일). </br>
                    이미 지정해놨지만 혹시 필요하다면 저장 파일명은 응답의 fileName을 사용하세요(예: 나답_나에게답하다_20251101-20251130.pdf). 브라우저로 열어 받으면 URL에 파일명이 지정돼 있어 자동 적용되고, 앱(Capacitor) 네이티브 저장은 헤더를 읽지 않으므로 이 fileName을 직접 지정해야 할듯 합니다. </br>
                    expiresAt은 다운로드 보관 만료 시각(완료 + 7일)입니다. 이 시각이 지나면 발급되지 않으므로(409 EXPIRED) 재생성이 필요합니다. </br>
                    아직 완료 전이면 409(NOT_COMPLETED)가 반환됩니다. </br>
                    발급은 유저당 1분에 20회로 제한되며, 초과하면 429(RATE_LIMITED)가 반환됩니다. 실제 다운로드 버튼을 누를 때만 1회 호출하면 정상 사용으로는 거의 닿지 않지만, 폴링할 때마다·화면이 리렌더될 때마다·아카이브 목록의 모든 항목에 대해 자동으로 이 API를 부르면 실수로 걸릴 수 있으니 주의하세요. 429가 나면 잠시 후 재시도하면 됩니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "다운로드 URL 발급 성공",
                            content = @Content(schema = @Schema(implementation = PdfExportDownloadResponse.class), mediaType = "application/json")
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(
                            responseCode = "403",
                            description = "- ErrorCode: PDF_EXPORT_ACCESS_FORBIDDEN - 본인 작업이 아님",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "- ErrorCode: PDF_EXPORT_JOB_NOT_FOUND - 작업을 찾을 수 없음",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = """
                                    - ErrorCode: PDF_EXPORT_NOT_COMPLETED - 아직 생성이 완료되지 않음(폴링으로 완료 확인 후 호출)
                                    - ErrorCode: PDF_EXPORT_EXPIRED - 보관 기간(7일)이 지나 만료됨(재생성 필요)
                                    """,
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "429",
                            description = "- ErrorCode: PDF_EXPORT_DOWNLOAD_RATE_LIMITED - 다운로드 URL 발급 요청이 너무 잦음(잠시 후 재시도)",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<ApiResponseDto<PdfExportDownloadResponse>> issuePdfExportDownloadUrl(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long jobId
    ) {
        PdfExportDownloadResponse response = pdfExportQueryService.issueDownloadUrl(principal.getId(), jobId);
        return ApiResponseEntity.ok(response);
    }
}
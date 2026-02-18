package com.devkor.ifive.nadab.domain.typereport.application;

import com.devkor.ifive.nadab.domain.typereport.api.dto.response.MyTypeReportResponse;
import com.devkor.ifive.nadab.domain.typereport.api.dto.response.TypeReportResponse;
import com.devkor.ifive.nadab.domain.typereport.application.mapper.TypeReportMapper;
import com.devkor.ifive.nadab.domain.typereport.core.repository.TypeReportRepository;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TypeReportQueryService {

    private final UserRepository userRepository;
    private final TypeReportRepository typeReportRepository;

    private final ProfileImageUrlBuilder imageUrlBuilder;

    public MyTypeReportResponse getMyTypeReport(Long userId, String interestCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        InterestCode code = InterestCode.fromString(interestCode);

        TypeReportResponse reportResponse =
                typeReportRepository.findByUserIdAndInterestCodeAndDeletedAtIsNull(
                                user.getId(),
                                code
                        )
                        .map(
                                report -> TypeReportMapper.toResponse(
                                        report,
                                        report.getAnalysisType(),
                                        imageUrlBuilder.buildAnalysisTypeImageUrl(report.getAnalysisType().getCode()))
                                )
                        .orElse(null);

        return new MyTypeReportResponse(reportResponse);
    }
}

package com.devkor.ifive.nadab.domain.typereport.application.mapper;

import com.devkor.ifive.nadab.domain.typereport.api.dto.response.TypeReportResponse;
import com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReport;
import com.devkor.ifive.nadab.domain.typereport.core.entity.TypeReportStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TypeReportMapperTest {

    @Test
    void toResponse_sanitizes_legacy_marks_tags_in_persona_content() {
        TypeReport report = mock(TypeReport.class);
        when(report.getStatus()).thenReturn(TypeReportStatus.COMPLETED);
        when(report.getPersona1Content()).thenReturn(
                "운동으로 자기 관리를 이어가며, "
                        + "<marks text=\"내적 만족감을 높여요\" marks=[\"BOLD\",\"HIGHLIGHT\"]>."
        );
        when(report.getPersona2Content()).thenReturn(
                "이는 <marks text=\"변화에 대한 긍정적 태도\" marks=[\"BOLD\",\"HIGHLIGHT\"]>를 바탕으로 해요."
        );

        TypeReportResponse response = TypeReportMapper.toResponse(report, null, null);

        assertThat(response.personaContent1())
                .isEqualTo("운동으로 자기 관리를 이어가며, 내적 만족감을 높여요.");
        assertThat(response.personaContent2())
                .isEqualTo("이는 변화에 대한 긍정적 태도를 바탕으로 해요.");
    }
}

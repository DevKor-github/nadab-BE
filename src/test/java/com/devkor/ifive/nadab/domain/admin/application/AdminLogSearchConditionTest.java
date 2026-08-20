package com.devkor.ifive.nadab.domain.admin.application;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

class AdminLogSearchConditionTest {

    @Test
    void normalizes_search_values_and_builds_latest_first_pageable() {
        AdminLogSearchCondition condition = AdminLogSearchCondition.of(
                2,
                30,
                "  nadab  ",
                "   "
        );

        assertThat(condition.page()).isEqualTo(2);
        assertThat(condition.size()).isEqualTo(30);
        assertThat(condition.nickname()).isEqualTo("nadab");
        assertThat(condition.email()).isNull();
        assertThat(condition.toPageable().getPageNumber()).isEqualTo(1);
        assertThat(condition.toPageable().getPageSize()).isEqualTo(30);
        assertThat(condition.toPageable().getSort().toList())
                .extracting(Sort.Order::getProperty, Sort.Order::getDirection)
                .containsExactly(
                        tuple("createdAt", Sort.Direction.DESC),
                        tuple("id", Sort.Direction.DESC)
                );
    }

    @Test
    void rejects_invalid_page_request() {
        assertThatThrownBy(() -> AdminLogSearchCondition.of(0, 20, null, null))
                .isInstanceOfSatisfying(BadRequestException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED));
        assertThatThrownBy(() -> AdminLogSearchCondition.of(1, 0, null, null))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> AdminLogSearchCondition.of(1, 51, null, null))
                .isInstanceOf(BadRequestException.class);
    }
}

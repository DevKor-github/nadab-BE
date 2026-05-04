package com.devkor.ifive.nadab.domain.question.core.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class UserDailyQuestionTest {

    @Test
    void create_초기_리롤_횟수는_5다() {
        // given
        LocalDate date = LocalDate.now();
        DailyQuestion dailyQuestion = new DailyQuestion();

        // when
        UserDailyQuestion userDailyQuestion = UserDailyQuestion.create(null, date, dailyQuestion);

        // then
        assertThat(userDailyQuestion.getRerollLeft()).isEqualTo(5);
        assertThat(userDailyQuestion.isRerollUsed()).isFalse();
    }

    @Test
    void rerollTo_호출시_rerollUsed는_true가_된다() {
        // given
        LocalDate date = LocalDate.now();
        UserDailyQuestion userDailyQuestion = UserDailyQuestion.create(null, date, new DailyQuestion());

        // when
        userDailyQuestion.rerollTo(new DailyQuestion());

        // then
        assertThat(userDailyQuestion.isRerollUsed()).isTrue();
    }
}

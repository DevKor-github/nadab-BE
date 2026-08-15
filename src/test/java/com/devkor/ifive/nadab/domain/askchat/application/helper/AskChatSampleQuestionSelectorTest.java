package com.devkor.ifive.nadab.domain.askchat.application.helper;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSampleQuestion;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AskChatSampleQuestionSelectorTest {

    private final AskChatSampleQuestionSelector selector = new AskChatSampleQuestionSelector();

    @Test
    void select_returns_same_questions_within_same_ten_minute_slot() {
        List<AskChatSampleQuestion> questions = questionsForAllCategories();

        var first = selector.select(1L, Instant.parse("2026-08-15T00:00:00Z"), questions);
        var last = selector.select(1L, Instant.parse("2026-08-15T00:09:59Z"), questions);

        assertThat(last).containsExactlyElementsOf(first);
    }

    @Test
    void select_rotates_questions_when_ten_minute_slot_changes() {
        List<AskChatSampleQuestion> questions = questionsForAllCategories();

        var before = selector.select(1L, Instant.parse("2026-08-15T00:09:59Z"), questions);
        var after = selector.select(1L, Instant.parse("2026-08-15T00:10:00Z"), questions);

        assertThat(after).isNotEqualTo(before);
        assertThat(after)
                .extracting(AskChatSampleQuestion::getInterestCode)
                .doesNotHaveDuplicates();
    }

    @Test
    void select_distributes_questions_by_user() {
        List<AskChatSampleQuestion> questions = questionsForAllCategories();
        Instant now = Instant.parse("2026-08-15T00:05:00Z");

        var firstUser = selector.select(1L, now, questions);
        var secondUser = selector.select(2L, now, questions);

        assertThat(secondUser).isNotEqualTo(firstUser);
    }

    @Test
    void select_is_independent_of_input_order() {
        List<AskChatSampleQuestion> questions = questionsForAllCategories();
        List<AskChatSampleQuestion> reversedQuestions = new ArrayList<>(questions);
        Collections.reverse(reversedQuestions);
        Instant now = Instant.parse("2026-08-15T00:05:00Z");

        var originalOrder = selector.select(1L, now, questions);
        var reversedOrder = selector.select(1L, now, reversedQuestions);

        assertThat(reversedOrder).containsExactlyElementsOf(originalOrder);
    }

    @Test
    void select_returns_available_categories_when_fewer_than_three_exist() {
        List<AskChatSampleQuestion> questions = List.of(
                question(InterestCode.PREFERENCE, "취향 질문", 2),
                question(InterestCode.VALUES, "가치관 질문", 1)
        );

        var selected = selector.select(1L, Instant.parse("2026-08-15T00:05:00Z"), questions);

        assertThat(selected).hasSize(2);
        assertThat(selected)
                .extracting(AskChatSampleQuestion::getInterestCode)
                .containsExactlyInAnyOrder(InterestCode.PREFERENCE, InterestCode.VALUES);
    }

    @Test
    void select_returns_empty_list_when_no_questions_exist() {
        var selected = selector.select(1L, Instant.parse("2026-08-15T00:05:00Z"), List.of());

        assertThat(selected).isEmpty();
    }

    private List<AskChatSampleQuestion> questionsForAllCategories() {
        List<AskChatSampleQuestion> questions = new ArrayList<>();
        int displayOrder = 1;
        for (InterestCode category : InterestCode.values()) {
            questions.add(question(category, category.name() + " 질문 A", displayOrder++));
            questions.add(question(category, category.name() + " 질문 B", displayOrder++));
        }
        return questions;
    }

    private AskChatSampleQuestion question(InterestCode category, String question, int displayOrder) {
        return AskChatSampleQuestion.create(category, question, displayOrder);
    }
}

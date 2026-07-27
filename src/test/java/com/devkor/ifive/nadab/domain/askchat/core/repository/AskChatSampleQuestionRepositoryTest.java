package com.devkor.ifive.nadab.domain.askchat.core.repository;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSampleQuestion;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AskChatSampleQuestionRepositoryTest extends PostgresIntegrationTestSupport {

    @Autowired
    AskChatSampleQuestionRepository askChatSampleQuestionRepository;

    @Test
    void find_active_sample_questions_ordered_by_display_order() {
        askChatSampleQuestionRepository.save(AskChatSampleQuestion.create(InterestCode.PREFERENCE, "내가 좋아하는 것은 뭐야?", 1002));
        askChatSampleQuestionRepository.save(AskChatSampleQuestion.create(InterestCode.RELATIONSHIP, "어떤 사람과 잘 맞을까?", 1001));

        var questions = askChatSampleQuestionRepository.findByActiveTrueOrderByDisplayOrderAsc();

        assertThat(questions)
                .extracting(AskChatSampleQuestion::getQuestion)
                .containsSubsequence(
                        "어떤 사람과 잘 맞을까?",
                        "내가 좋아하는 것은 뭐야?"
                );
    }
}

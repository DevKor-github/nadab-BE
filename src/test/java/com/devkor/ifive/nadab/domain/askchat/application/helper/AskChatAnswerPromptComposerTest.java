package com.devkor.ifive.nadab.domain.askchat.application.helper;

import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerConversationMessage;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerPromptContext;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerReferenceDocument;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageRole;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatRagDocumentSourceType;
import com.devkor.ifive.nadab.domain.askchat.core.properties.AskChatAnswerProperties;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AskChatAnswerPromptComposerTest {

    @Test
    void compose_builds_prompt_with_question_recent_messages_and_reference_documents() {
        AskChatAnswerPromptComposer composer = new AskChatAnswerPromptComposer(properties());
        AskChatAnswerPromptContext context = new AskChatAnswerPromptContext(
                1L,
                10L,
                "나는 어떤 사람에 가까워?",
                List.of(
                        new AskChatAnswerConversationMessage(AskChatMessageRole.USER, "내 강점은 뭐야?"),
                        new AskChatAnswerConversationMessage(AskChatMessageRole.ASSISTANT, "꾸준함이 보여요.")
                ),
                List.of(new AskChatAnswerReferenceDocument(
                        100L,
                        AskChatRagDocumentSourceType.ANSWER_ENTRY,
                        200L,
                        InterestCode.VALUES,
                        "사용자는 기록에서 꾸준함과 책임감을 중요하게 말했다.",
                        0.18
                ))
        );

        var prompt = composer.compose(context);

        assertThat(prompt.systemPrompt()).contains("JSON 형식만 반환");
        assertThat(prompt.userPrompt())
                .contains("나는 어떤 사람에 가까워?")
                .contains("USER: 내 강점은 뭐야?")
                .contains("ASSISTANT: 꾸준함이 보여요.")
                .contains("documentId=100")
                .contains("ANSWER_ENTRY")
                .contains("VALUES")
                .contains("사용자는 기록에서 꾸준함과 책임감을 중요하게 말했다.")
                .contains("followUpQuestions는 2개 이하");
    }

    @Test
    void compose_marks_empty_context_when_recent_messages_and_reference_documents_are_empty() {
        AskChatAnswerPromptComposer composer = new AskChatAnswerPromptComposer(properties());
        AskChatAnswerPromptContext context = new AskChatAnswerPromptContext(
                1L,
                10L,
                "요즘 내가 반복하는 패턴이 있어?",
                null,
                null
        );

        var prompt = composer.compose(context);

        assertThat(prompt.userPrompt())
                .contains("최근 대화가 없습니다.")
                .contains("검색된 사용자 기록이 없습니다.");
    }

    private AskChatAnswerProperties properties() {
        AskChatAnswerProperties properties = new AskChatAnswerProperties();
        properties.setPromptVersion(1);
        properties.setFollowUpQuestionCount(2);
        return properties;
    }
}

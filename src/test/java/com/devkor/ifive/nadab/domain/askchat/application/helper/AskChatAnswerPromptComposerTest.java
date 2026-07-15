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
    void augment_builds_prompt_with_friendly_tone_and_conversation_order_rules() {
        AskChatAnswerPromptComposer composer = new AskChatAnswerPromptComposer(properties());
        AskChatAnswerPromptContext context = new AskChatAnswerPromptContext(
                1L,
                10L,
                "내가 방금 한 질문은 무엇이지?",
                List.of(
                        new AskChatAnswerConversationMessage(AskChatMessageRole.USER, "나는 어떤 사람이야?"),
                        new AskChatAnswerConversationMessage(AskChatMessageRole.ASSISTANT, "말해준 걸 보면 관계를 중요하게 여기는 편으로 보여요.")
                ),
                List.of(new AskChatAnswerReferenceDocument(
                        100L,
                        AskChatRagDocumentSourceType.ANSWER_ENTRY,
                        200L,
                        InterestCode.VALUES,
                        "사용자는 기록에서 솔직함과 책임감을 중요하게 말한 적이 있다.",
                        0.18
                ))
        );

        var prompt = composer.augment(context);

        assertThat(prompt.systemPrompt())
                .contains("친절하고 편안한 한국어 구어체")
                .contains("\"당신은\", \"사용자께서는\", \"사용자는\" 같은 딱딱하고 AI스러운 표현은 사용하지 마세요")
                .contains("[최근 대화]의 마지막 USER 메시지를 기준으로 답하세요")
                .contains("JSON 형식만 반환");
        assertThat(prompt.userPrompt())
                .contains("내가 방금 한 질문은 무엇이지?")
                .contains("USER: 나는 어떤 사람이야?")
                .contains("ASSISTANT: 말해준 걸 보면 관계를 중요하게 여기는 편으로 보여요.")
                .contains("documentId=100")
                .contains("ANSWER_ENTRY")
                .contains("VALUES")
                .contains("사용자는 기록에서 솔직함과 책임감을 중요하게 말한 적이 있다.")
                .contains("followUpQuestions는 2개 이하")
                .contains("최근 대화 목록에는 현재 질문을 포함하지 않았습니다")
                .contains("최근 대화의 마지막 USER 메시지를 답하세요");
    }

    @Test
    void augment_marks_empty_context_when_recent_messages_and_reference_documents_are_empty() {
        AskChatAnswerPromptComposer composer = new AskChatAnswerPromptComposer(properties());
        AskChatAnswerPromptContext context = new AskChatAnswerPromptContext(
                1L,
                10L,
                "요즘 내가 반복하는 패턴이 있어?",
                null,
                null
        );

        var prompt = composer.augment(context);

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

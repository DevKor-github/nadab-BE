package com.devkor.ifive.nadab.domain.askchat.application.helper;

import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerConversationMessage;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerPromptContext;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerReferenceDocument;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatMessageRole;
import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatRagDocumentSourceType;
import com.devkor.ifive.nadab.domain.askchat.core.properties.AskChatAnswerProperties;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.global.core.prompt.askchat.AskChatAnswerPromptLoader;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AskChatAnswerPromptComposerTest {

    @Test
    void augment_builds_prompt_from_loaded_templates() {
        AskChatAnswerPromptComposer composer = new AskChatAnswerPromptComposer(properties(), promptLoader());
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

        assertThat(prompt.systemPrompt()).isEqualTo("system template");
        assertThat(prompt.userPrompt())
                .contains("프롬프트 버전: 1")
                .contains("내가 방금 한 질문은 무엇이지?")
                .contains("USER: 나는 어떤 사람이야?")
                .contains("ASSISTANT: 말해준 걸 보면 관계를 중요하게 여기는 편으로 보여요.")
                .contains("documentId=100")
                .contains("ANSWER_ENTRY")
                .contains("VALUES")
                .contains("사용자는 기록에서 솔직함과 책임감을 중요하게 말한 적이 있다.")
                .contains("followUpQuestions는 2개 이하");
        assertThat(prompt.userPrompt())
                .doesNotContain("{promptVersion}")
                .doesNotContain("{question}")
                .doesNotContain("{recentMessages}")
                .doesNotContain("{referenceDocuments}")
                .doesNotContain("{followUpQuestionCount}");
    }

    @Test
    void augment_marks_empty_context_when_recent_messages_and_reference_documents_are_empty() {
        AskChatAnswerPromptComposer composer = new AskChatAnswerPromptComposer(properties(), promptLoader());
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

    private AskChatAnswerPromptLoader promptLoader() {
        return new AskChatAnswerPromptLoader() {
            @Override
            public String loadSystemPrompt() {
                return "system template";
            }

            @Override
            public String loadUserPrompt() {
                return """
                        프롬프트 버전: {promptVersion}

                        [현재 질문]
                        {question}

                        [최근 대화]
                        {recentMessages}

                        [검색된 사용자 기록]
                        {referenceDocuments}

                        [이번 답변에서 지켜야 할 세부 조건]
                        - followUpQuestions는 {followUpQuestionCount}개 이하
                        """;
            }
        };
    }
}

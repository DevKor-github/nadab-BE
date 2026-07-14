package com.devkor.ifive.nadab.domain.askchat.application.helper;

import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerConversationMessage;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerPrompt;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerPromptContext;
import com.devkor.ifive.nadab.domain.askchat.core.dto.AskChatAnswerReferenceDocument;
import com.devkor.ifive.nadab.domain.askchat.core.properties.AskChatAnswerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AskChatAnswerPromptComposer {

    private static final String NO_REFERENCE_DOCUMENT = "검색된 사용자 기록이 없습니다.";
    private static final String NO_RECENT_MESSAGE = "최근 대화가 없습니다.";

    private final AskChatAnswerProperties properties;

    public AskChatAnswerPrompt compose(AskChatAnswerPromptContext context) {
        return new AskChatAnswerPrompt(
                systemPrompt(),
                userPrompt(context)
        );
    }

    private String systemPrompt() {
        return """
                당신은 사용자의 누적 답변과 리포트 기록을 바탕으로 자기이해를 돕는 상담형 챗봇입니다.
                반드시 제공된 사용자 기록과 최근 대화 맥락 안에서만 답변하세요.
                사용자의 성격, 관계, 가치관, 루틴을 단정하지 말고 근거가 약하면 조심스럽게 표현하세요.
                답변은 한국어 존댓말로 작성하고, 따뜻하지만 과장되지 않게 말하세요.
                응답은 아래 JSON 형식만 반환하세요.
                {
                  "answer": "사용자에게 보여줄 답변",
                  "followUpQuestions": ["후속 질문 1", "후속 질문 2"]
                }
                """;
    }

    private String userPrompt(AskChatAnswerPromptContext context) {
        return """
                [프롬프트 버전]
                %d

                [현재 질문]
                %s

                [최근 대화]
                %s

                [검색된 사용자 기록]
                %s

                [작성 조건]
                - answer는 500자 이내로 작성하세요.
                - followUpQuestions는 %d개 이하로 작성하세요.
                - 검색된 사용자 기록이 부족하면 부족하다고 말하고, 현재 질문에 답할 수 있는 범위만 답하세요.
                - 사용자 기록의 원문을 길게 그대로 복사하지 마세요.
                """.formatted(
                properties.getPromptVersion(),
                context.question(),
                formatRecentMessages(context.recentMessages()),
                formatReferenceDocuments(context.referenceDocuments()),
                properties.getFollowUpQuestionCount()
        );
    }

    private String formatRecentMessages(List<AskChatAnswerConversationMessage> messages) {
        if (messages.isEmpty()) {
            return NO_RECENT_MESSAGE;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < messages.size(); i++) {
            AskChatAnswerConversationMessage message = messages.get(i);
            builder.append(i + 1)
                    .append(". ")
                    .append(message.role())
                    .append(": ")
                    .append(message.content())
                    .append(System.lineSeparator());
        }
        return builder.toString().trim();
    }

    private String formatReferenceDocuments(List<AskChatAnswerReferenceDocument> documents) {
        if (documents.isEmpty()) {
            return NO_REFERENCE_DOCUMENT;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < documents.size(); i++) {
            AskChatAnswerReferenceDocument document = documents.get(i);
            builder.append(i + 1)
                    .append(". documentId=")
                    .append(document.documentId())
                    .append(", sourceType=")
                    .append(document.sourceType())
                    .append(", interestCode=")
                    .append(document.interestCode())
                    .append(", distance=")
                    .append(document.distance())
                    .append(System.lineSeparator())
                    .append(document.content())
                    .append(System.lineSeparator());
        }
        return builder.toString().trim();
    }
}

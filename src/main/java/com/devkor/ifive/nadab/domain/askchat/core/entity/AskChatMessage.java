package com.devkor.ifive.nadab.domain.askchat.core.entity;

import com.devkor.ifive.nadab.global.infra.llm.LlmProvider;
import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ask_chat_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AskChatMessage extends CreatableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "session_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_ask_chat_messages_session")
    )
    private AskChatSession session;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 16)
    private AskChatMessageRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private AskChatMessageStatus status;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "llm_provider", length = 32)
    private LlmProvider llmProvider;

    @Column(name = "llm_model", length = 128)
    private String llmModel;

    @Column(name = "input_tokens")
    private Long inputTokens;

    @Column(name = "output_tokens")
    private Long outputTokens;

    @Column(name = "total_tokens")
    private Long totalTokens;

    @Column(name = "thinking_tokens")
    private Long thinkingTokens;

    @Column(name = "error_code", length = 128)
    private String errorCode;

    public static AskChatMessage createUserMessage(AskChatSession session, String content) {
        AskChatMessage message = create(session, AskChatMessageRole.USER, content);
        message.status = AskChatMessageStatus.COMPLETED;
        return message;
    }

    public static AskChatMessage createAssistantMessage(
            AskChatSession session,
            String content,
            LlmProvider llmProvider,
            String llmModel,
            Long inputTokens,
            Long outputTokens,
            Long totalTokens,
            Long thinkingTokens
    ) {
        AskChatMessage message = create(session, AskChatMessageRole.ASSISTANT, content);
        message.status = AskChatMessageStatus.COMPLETED;
        message.llmProvider = llmProvider;
        message.llmModel = llmModel;
        message.inputTokens = inputTokens;
        message.outputTokens = outputTokens;
        message.totalTokens = totalTokens;
        message.thinkingTokens = thinkingTokens;
        return message;
    }

    public static AskChatMessage createFailedAssistantMessage(
            AskChatSession session,
            String content,
            LlmProvider llmProvider,
            String llmModel,
            String errorCode
    ) {
        AskChatMessage message = create(session, AskChatMessageRole.ASSISTANT, content);
        message.status = AskChatMessageStatus.FAILED;
        message.llmProvider = llmProvider;
        message.llmModel = llmModel;
        message.errorCode = errorCode;
        return message;
    }

    private static AskChatMessage create(AskChatSession session, AskChatMessageRole role, String content) {
        AskChatMessage message = new AskChatMessage();
        message.session = session;
        message.role = role;
        message.content = content;
        return message;
    }
}

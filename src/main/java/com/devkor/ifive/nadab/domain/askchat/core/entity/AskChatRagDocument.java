package com.devkor.ifive.nadab.domain.askchat.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.AuditableEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(
        name = "ask_chat_rag_documents",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_ask_chat_rag_documents_source_version",
                        columnNames = {"source_type", "source_id", "embedding_version"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AskChatRagDocument extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_ask_chat_rag_documents_user")
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 32)
    private AskChatRagDocumentSourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_code", length = 32)
    private InterestCode interestCode;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Type(JsonType.class)
    @Column(name = "metadata", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Column(name = "embedding_model", nullable = false, length = 128)
    private String embeddingModel;

    @Column(name = "embedding_version", nullable = false)
    private int embeddingVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "embedding_status", nullable = false, length = 16)
    private AskChatRagEmbeddingStatus embeddingStatus;

    @Column(name = "error_code", length = 128)
    private String errorCode;

    @Column(name = "embedded_at")
    private OffsetDateTime embeddedAt;

    public static AskChatRagDocument createPending(
            User user,
            AskChatRagDocumentSourceType sourceType,
            Long sourceId,
            InterestCode interestCode,
            String content,
            Map<String, Object> metadata,
            String embeddingModel,
            int embeddingVersion
    ) {
        AskChatRagDocument document = new AskChatRagDocument();
        document.user = user;
        document.sourceType = sourceType;
        document.sourceId = sourceId;
        document.interestCode = interestCode;
        document.content = content;
        document.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        document.embeddingModel = embeddingModel;
        document.embeddingVersion = embeddingVersion;
        document.embeddingStatus = AskChatRagEmbeddingStatus.PENDING;
        return document;
    }

    public void markCompleted() {
        this.embeddingStatus = AskChatRagEmbeddingStatus.COMPLETED;
        this.errorCode = null;
        this.embeddedAt = OffsetDateTime.now();
    }

    public void markFailed(String errorCode) {
        this.embeddingStatus = AskChatRagEmbeddingStatus.FAILED;
        this.errorCode = errorCode;
    }
}

package com.devkor.ifive.nadab.domain.askchat.core.entity;

import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(
        name = "ask_chat_message_references",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_ask_chat_message_references_message_document",
                        columnNames = {"message_id", "rag_document_id"}
                ),
                @UniqueConstraint(
                        name = "uq_ask_chat_message_references_message_order",
                        columnNames = {"message_id", "display_order"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AskChatMessageReference extends CreatableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "message_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_ask_chat_message_references_message")
    )
    private AskChatMessage message;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "rag_document_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_ask_chat_message_references_rag_document")
    )
    private AskChatRagDocument ragDocument;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public static AskChatMessageReference of(
            AskChatMessage message,
            AskChatRagDocument ragDocument,
            int displayOrder
    ) {
        AskChatMessageReference reference = new AskChatMessageReference();
        reference.message = message;
        reference.ragDocument = ragDocument;
        reference.displayOrder = displayOrder;
        return reference;
    }
}

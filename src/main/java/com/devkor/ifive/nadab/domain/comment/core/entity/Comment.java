package com.devkor.ifive.nadab.domain.comment.core.entity;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "daily_report_id", nullable = false)
    private DailyReport dailyReport;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "is_secret", nullable = false)
    private boolean secret;

    public static Comment createTopLevel(DailyReport dailyReport, User author, String content, boolean isSecret) {
        Comment comment = new Comment();
        comment.dailyReport = dailyReport;
        comment.author = author;
        comment.content = content;
        comment.secret = isSecret;
        return comment;
    }

    public static Comment createSubComment(
            User author, Comment parentComment, String content, boolean isSecret) {
        Comment comment = new Comment();
        comment.dailyReport = parentComment.dailyReport;
        comment.author = author;
        comment.parentComment = parentComment;
        comment.content = content;
        comment.secret = isSecret;
        return comment;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public boolean isTopLevel() {
        return parentComment == null;
    }
}
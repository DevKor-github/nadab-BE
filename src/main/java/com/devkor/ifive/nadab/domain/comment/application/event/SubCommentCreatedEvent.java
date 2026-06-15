package com.devkor.ifive.nadab.domain.comment.application.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SubCommentCreatedEvent {

    private final Long subCommentId;
    private final Long dailyReportId;
    private final Long authorId;
    private final Long parentCommentId;
    private final Long parentCommentAuthorId;
    private final Long reportOwnerId;
    private final String content;
    private final boolean secret;
    private final boolean parentSecret;
}
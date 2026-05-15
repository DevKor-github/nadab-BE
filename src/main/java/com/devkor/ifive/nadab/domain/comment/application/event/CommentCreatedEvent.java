package com.devkor.ifive.nadab.domain.comment.application.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CommentCreatedEvent {

    private final Long commentId;
    private final Long dailyReportId;
    private final Long authorId;
    private final Long reportOwnerId;
    private final String content;
}
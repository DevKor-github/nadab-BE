package com.devkor.ifive.nadab.domain.askchat.core.entity;

import com.devkor.ifive.nadab.domain.user.core.entity.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AskChatSessionTest {

    @Test
    void completeAnsweredTurn_keeps_session_active_before_limit() {
        AskChatSession session = AskChatSession.start(mock(User.class));

        session.completeAnsweredTurn(15);

        assertThat(session.getAnsweredTurnCount()).isEqualTo(1);
        assertThat(session.getStatus()).isEqualTo(AskChatSessionStatus.ACTIVE);
        assertThat(session.getEndedAt()).isNull();
    }

    @Test
    void completeAnsweredTurn_ends_session_when_limit_is_reached() {
        AskChatSession session = AskChatSession.start(mock(User.class));

        for (int i = 0; i < 15; i++) {
            session.completeAnsweredTurn(15);
        }

        assertThat(session.getAnsweredTurnCount()).isEqualTo(15);
        assertThat(session.getStatus()).isEqualTo(AskChatSessionStatus.ENDED);
        assertThat(session.getEndedAt()).isNotNull();
    }
}

package com.devkor.ifive.nadab.domain.monthlyreport;

import com.devkor.ifive.nadab.domain.comment.core.entity.Comment;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.AnswerEntry;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReportStatus;
import com.devkor.ifive.nadab.domain.friend.core.entity.Friendship;
import com.devkor.ifive.nadab.domain.like.core.entity.DailyReportLike;
import com.devkor.ifive.nadab.domain.moderation.core.entity.UserBlock;
import com.devkor.ifive.nadab.domain.monthlyreport.core.dto.MonthlySocialInteractionCountDto;
import com.devkor.ifive.nadab.domain.monthlyreport.core.repository.MonthlySocialQueryRepository;
import com.devkor.ifive.nadab.domain.question.core.entity.DailyQuestion;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.infra.builder.UserBuilder;
import com.devkor.ifive.nadab.infra.db.PostgresIntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MonthlySocialQueryRepositoryTest extends PostgresIntegrationTestSupport {

    @Autowired
    MonthlySocialQueryRepository monthlySocialQueryRepository;

    @Autowired
    TestEntityManager em;

    DailyQuestion question;
    OffsetDateTime startAt;
    OffsetDateTime endAt;

    @BeforeEach
    void setUp() {
        question = em.getEntityManager()
                .createQuery("select q from DailyQuestion q where q.deletedAt is null", DailyQuestion.class)
                .setMaxResults(1)
                .getSingleResult();
        startAt = OffsetDateTime.now().minusDays(1);
        endAt = OffsetDateTime.now().plusDays(1);
    }

    @Test
    void counts_only_likes_received_from_current_available_friends() {
        User owner = user("owner");
        User friend = user("가나다");
        User blockedFriend = user("blocked");
        User deletedFriend = user("deleted");
        User nonFriend = user("stranger");
        User otherOwner = user("otherOwner");
        acceptFriendship(owner, friend);
        acceptFriendship(owner, blockedFriend);
        acceptFriendship(owner, deletedFriend);
        em.persist(UserBlock.create(blockedFriend, owner));

        DailyReport firstReport = report(owner, LocalDate.of(2026, 6, 1));
        DailyReport secondReport = report(owner, LocalDate.of(2026, 6, 2));
        DailyReport outsideRangeReport = report(owner, LocalDate.of(2026, 6, 3));
        DailyReport otherReport = report(otherOwner, LocalDate.of(2026, 6, 1));

        em.persist(DailyReportLike.create(friend, firstReport));
        em.persist(DailyReportLike.create(friend, secondReport));
        DailyReportLike outsideRangeLike = DailyReportLike.create(friend, outsideRangeReport);
        em.persist(outsideRangeLike);
        em.persist(DailyReportLike.create(friend, otherReport));
        em.persist(DailyReportLike.create(blockedFriend, firstReport));
        em.persist(DailyReportLike.create(deletedFriend, firstReport));
        em.persist(DailyReportLike.create(nonFriend, firstReport));
        deletedFriend.softDelete();

        em.flush();
        em.getEntityManager().createNativeQuery("UPDATE daily_report_likes SET created_at = :createdAt WHERE id = :id")
                .setParameter("createdAt", startAt.minusHours(1))
                .setParameter("id", outsideRangeLike.getId())
                .executeUpdate();
        em.clear();

        List<MonthlySocialInteractionCountDto> result = monthlySocialQueryRepository.countReceivedLikesByFriend(
                owner.getId(), startAt, endAt
        );

        assertThat(result).singleElement().satisfies(row -> {
            assertThat(row.userId()).isEqualTo(friend.getId());
            assertThat(row.nickname()).isEqualTo("가나다");
            assertThat(row.interactionCount()).isEqualTo(2);
        });
    }

    @Test
    void counts_active_comments_and_subcomments_received_from_current_available_friends() {
        User owner = user("owner");
        User friend = user("가나다");
        User blockedFriend = user("blocked");
        User deletedFriend = user("deleted");
        User nonFriend = user("stranger");
        User otherOwner = user("otherOwner");
        acceptFriendship(owner, friend);
        acceptFriendship(owner, blockedFriend);
        acceptFriendship(owner, deletedFriend);
        em.persist(UserBlock.create(owner, blockedFriend));

        DailyReport report = report(owner, LocalDate.of(2026, 6, 1));
        DailyReport otherReport = report(otherOwner, LocalDate.of(2026, 6, 1));
        Comment topLevel = Comment.createTopLevel(report, friend, "comment", false);
        em.persist(topLevel);
        em.persist(Comment.createSubComment(friend, topLevel, "subcomment", false));
        Comment deletedComment = Comment.createTopLevel(report, friend, "deleted", false);
        em.persist(deletedComment);
        deletedComment.softDelete();
        Comment outsideRangeComment = Comment.createTopLevel(report, friend, "old", false);
        em.persist(outsideRangeComment);
        em.persist(Comment.createTopLevel(otherReport, friend, "other report", false));
        em.persist(Comment.createTopLevel(report, blockedFriend, "blocked", false));
        em.persist(Comment.createTopLevel(report, deletedFriend, "withdrawn", false));
        em.persist(Comment.createTopLevel(report, nonFriend, "stranger", false));
        deletedFriend.softDelete();

        em.flush();
        em.getEntityManager().createNativeQuery("UPDATE comments SET created_at = :createdAt WHERE id = :id")
                .setParameter("createdAt", startAt.minusHours(1))
                .setParameter("id", outsideRangeComment.getId())
                .executeUpdate();
        em.clear();

        List<MonthlySocialInteractionCountDto> result = monthlySocialQueryRepository.countReceivedCommentsByFriend(
                owner.getId(), startAt, endAt
        );

        assertThat(result).singleElement().satisfies(row -> {
            assertThat(row.userId()).isEqualTo(friend.getId());
            assertThat(row.nickname()).isEqualTo("가나다");
            assertThat(row.interactionCount()).isEqualTo(2);
        });
    }

    private User user(String nickname) {
        User user = new UserBuilder(em).build();
        user.updateNickname(nickname);
        return user;
    }

    private void acceptFriendship(User first, User second) {
        Friendship friendship = Friendship.createPending(first, second);
        friendship.accept();
        em.persist(friendship);
    }

    private DailyReport report(User owner, LocalDate date) {
        AnswerEntry answerEntry = AnswerEntry.create(owner, question, "answer", date, null);
        em.persist(answerEntry);
        DailyReport report = DailyReport.create(answerEntry, null, "report", date, DailyReportStatus.COMPLETED);
        em.persist(report);
        return report;
    }
}

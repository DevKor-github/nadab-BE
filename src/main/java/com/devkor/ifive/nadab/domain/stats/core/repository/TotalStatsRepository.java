package com.devkor.ifive.nadab.domain.stats.core.repository;

import com.devkor.ifive.nadab.domain.stats.core.dto.total.LabelCountDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TotalStatsRepository {

    private final EntityManager em;

    /** 총 유저 수 (가입 완료 + 미삭제) */
    public long countTotalUsers() {
        return em.createQuery("""
                select count(u.id)
                from User u
                where u.deletedAt is null
                  and u.signupStatus = com.devkor.ifive.nadab.domain.user.core.entity.SignupStatusType.COMPLETED
                """, Long.class)
                .getSingleResult();
    }

    /** 소셜별 유저 수 (가입 완료 + 미삭제, providerType 기준) */
    public List<Object[]> countUsersByProvider() {
        // row[0] = ProviderType, row[1] = Long
        return em.createQuery("""
                select sa.providerType, count(distinct u.id)
                from SocialAccount sa
                join sa.user u
                where u.deletedAt is null
                  and u.signupStatus = com.devkor.ifive.nadab.domain.user.core.entity.SignupStatusType.COMPLETED
                group by sa.providerType
                order by sa.providerType
                """, Object[].class)
                .getResultList();
    }

    /** 일반 유저 수 = 가입 완료 + 미삭제 & social_account 없는 유저 */
    public long countNormalUsers() {
        return em.createQuery("""
                select count(u.id)
                from User u
                where u.deletedAt is null
                  and u.signupStatus = com.devkor.ifive.nadab.domain.user.core.entity.SignupStatusType.COMPLETED
                  and not exists (
                      select 1
                      from SocialAccount sa
                      where sa.user = u
                  )
                """, Long.class)
                .getSingleResult();
    }

    /** 주제별 선택 수 (user_interests) - 0도 포함되게 Interest 기준 LEFT JOIN */
    public List<LabelCountDto> countInterestSelections() {
        return em.createQuery("""
                select new com.devkor.ifive.nadab.domain.stats.core.dto.total.LabelCountDto(
                    cast(i.code as string),
                    count(ui.id)
                )
                from Interest i
                left join UserInterest ui on ui.interest = i
                group by i.code
                order by i.code
                """, LabelCountDto.class)
                .getResultList();
    }

    /** 주제별 COMPLETED 일간 리포트 생성 수 - 0도 포함되게 Interest 기준 LEFT JOIN */
    public List<LabelCountDto> countCompletedDailyReportsByInterest() {
        return em.createQuery("""
                select new com.devkor.ifive.nadab.domain.stats.core.dto.total.LabelCountDto(
                    cast(i.code as string),
                    count(dr.id)
                )
                from Interest i
                left join DailyQuestion dq on dq.interest = i
                left join AnswerEntry ae on ae.question = dq
                left join DailyReport dr on dr.answerEntry = ae
                                   and dr.status = 'COMPLETED'
                group by i.code
                order by i.code
                """, LabelCountDto.class)
                .getResultList();
    }
}

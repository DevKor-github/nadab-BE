package com.devkor.ifive.nadab.domain.like.core.entity;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.DailyReport;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.global.shared.entity.CreatableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "daily_report_likes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyReportLike extends CreatableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "daily_report_id", nullable = false)
    private DailyReport dailyReport;

    public static DailyReportLike create(User user, DailyReport dailyReport) {
        DailyReportLike like = new DailyReportLike();
        like.user = user;
        like.dailyReport = dailyReport;
        return like;
    }
}
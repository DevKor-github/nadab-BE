package com.devkor.ifive.nadab.domain.user.core.repository;

import com.devkor.ifive.nadab.domain.user.core.dto.UserSearchDto;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 조회
    Optional<User> findByEmail(String email);

    // 이메일 중복 체크
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    // 닉네임으로 사용자 조회 (탈퇴 유저 제외)
    Optional<User> findByNicknameAndDeletedAtIsNull(String nickname);

    // 사용자 검색 + 친구 관계 정보 조회
    @Query("""
        select new com.devkor.ifive.nadab.domain.user.core.dto.UserSearchDto(
            u.id,
            u.nickname,
            u.profileImageKey,
            u.defaultProfileType,
            coalesce(f1.id, f2.id),
            coalesce(f1.status, f2.status),
            case when f1.id is not null then true else false end
        )
        from User u
        left join Friendship f1 on f1.user1.id = :userId and f1.user2.id = u.id
        left join Friendship f2 on f2.user2.id = :userId and f2.user1.id = u.id
        where u.nickname like :keyword escape '\\'
          and u.deletedAt is null
          and (:cursor is null or u.nickname > :cursor)
          and (:excludeUserIds is null or u.id not in :excludeUserIds)
        order by
          case
            when u.nickname = :rawKeyword then 1
            when u.nickname like :prefixKeyword escape '\\' then 2
            else 3
          end asc,
          u.nickname asc
        """)
    List<UserSearchDto> searchUsersWithRelationship(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("rawKeyword") String rawKeyword,
            @Param("prefixKeyword") String prefixKeyword,
            @Param("cursor") String cursor,
            @Param("excludeUserIds") List<Long> excludeUserIds,
            Pageable pageable
    );

    @Modifying
    @Query("DELETE FROM User u WHERE u.deletedAt IS NOT NULL AND u.deletedAt < :expirationDate")
    int deleteOldWithdrawnUsers(@Param("expirationDate") OffsetDateTime expirationDate);
}
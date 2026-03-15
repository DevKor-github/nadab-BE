package com.devkor.ifive.nadab.domain.friend.core.repository;

import com.devkor.ifive.nadab.domain.friend.core.entity.FriendSearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendSearchHistoryRepository extends JpaRepository<FriendSearchHistory, Long> {

    @Query("""
        select h from FriendSearchHistory h
        where h.user.id = :userId
        and h.searchedUser.deletedAt is null
        order by h.updatedAt desc
        limit 5
        """)
    List<FriendSearchHistory> findTop5ByUser_IdOrderByUpdatedAtDesc(@Param("userId") Long userId);

    @Query("""
        select h from FriendSearchHistory h
        where h.user.id = :userId and h.searchedUser.id = :searchedUserId
        """)
    Optional<FriendSearchHistory> findByUserIdAndSearchedUserId(
            @Param("userId") Long userId,
            @Param("searchedUserId") Long searchedUserId
    );

    @Query("SELECT COUNT(h) FROM FriendSearchHistory h WHERE h.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE FriendSearchHistory h SET h.updatedAt = CURRENT_TIMESTAMP WHERE h.id = :id")
    void updateTimestamp(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        delete from FriendSearchHistory h
        where h.user.id = :userId and h.searchedUser.id = :searchedUserId
        """)
    void deleteByUserIdAndSearchedUserId(
            @Param("userId") Long userId,
            @Param("searchedUserId") Long searchedUserId
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM FriendSearchHistory h WHERE h.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        delete from friend_search_histories
        where id in (
            select id from (
                select id from friend_search_histories
                where user_id = :userId
                order by updated_at desc
                offset 100
            ) as old_histories
        )
        """, nativeQuery = true)
    int deleteOldHistoriesExceedingLimit(@Param("userId") Long userId);
}
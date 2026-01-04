package com.devkor.ifive.nadab.domain.search.core.repository;

import com.devkor.ifive.nadab.domain.search.core.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    List<SearchHistory> findTop10ByUserIdOrderByUpdatedAtDesc(Long userId);

    Optional<SearchHistory> findByUserIdAndKeyword(Long userId, String keyword);

    @Query("SELECT COUNT(h) FROM SearchHistory h WHERE h.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE SearchHistory h SET h.updatedAt = CURRENT_TIMESTAMP WHERE h.id = :id")
    void updateTimestamp(@Param("id") Long id);

    @Query("SELECT h FROM SearchHistory h WHERE h.user.id = :userId ORDER BY h.updatedAt DESC")
    List<SearchHistory> findAllByUserIdOrderByUpdatedAtDesc(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM SearchHistory h WHERE h.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
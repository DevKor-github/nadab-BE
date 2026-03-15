package com.devkor.ifive.nadab.domain.dailyreport.core.repository;

import com.devkor.ifive.nadab.domain.dailyreport.core.entity.Emotion;
import com.devkor.ifive.nadab.domain.dailyreport.core.entity.EmotionName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmotionRepository extends JpaRepository<Emotion,Long> {

    Optional<Emotion> findByName(EmotionName name);
}

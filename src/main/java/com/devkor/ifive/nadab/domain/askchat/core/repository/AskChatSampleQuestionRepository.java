package com.devkor.ifive.nadab.domain.askchat.core.repository;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSampleQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AskChatSampleQuestionRepository extends JpaRepository<AskChatSampleQuestion, Long> {

    List<AskChatSampleQuestion> findByActiveTrueOrderByDisplayOrderAsc();
}

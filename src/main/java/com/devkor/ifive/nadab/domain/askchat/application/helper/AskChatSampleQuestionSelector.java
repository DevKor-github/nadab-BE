package com.devkor.ifive.nadab.domain.askchat.application.helper;

import com.devkor.ifive.nadab.domain.askchat.core.entity.AskChatSampleQuestion;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class AskChatSampleQuestionSelector {

    private static final int SAMPLE_QUESTION_SIZE = 3;
    private static final long ROTATION_INTERVAL_SECONDS = Duration.ofMinutes(10).toSeconds();
    private static final long CATEGORY_SALT = 0x9E3779B97F4A7C15L;

    public List<AskChatSampleQuestion> select(
            Long userId,
            Instant now,
            List<AskChatSampleQuestion> sampleQuestions
    ) {
        Map<InterestCode, List<AskChatSampleQuestion>> questionsByCategory = groupByCategory(sampleQuestions);
        if (questionsByCategory.isEmpty()) {
            return List.of();
        }

        long rotationSlot = Math.floorDiv(now.getEpochSecond(), ROTATION_INTERVAL_SECONDS);
        List<InterestCode> categories = orderedCategories(userId, questionsByCategory);
        int categoryStartIndex = rotatedIndex(mix(userId), rotationSlot, categories.size());
        int resultSize = Math.min(SAMPLE_QUESTION_SIZE, categories.size());

        List<AskChatSampleQuestion> selectedQuestions = new ArrayList<>(resultSize);
        for (int index = 0; index < resultSize; index++) {
            InterestCode category = categories.get((categoryStartIndex + index) % categories.size());
            List<AskChatSampleQuestion> categoryQuestions = questionsByCategory.get(category);
            int questionIndex = rotatedIndex(
                    mix(userId ^ (CATEGORY_SALT * (category.ordinal() + 1L))),
                    rotationSlot,
                    categoryQuestions.size()
            );
            selectedQuestions.add(categoryQuestions.get(questionIndex));
        }

        return List.copyOf(selectedQuestions);
    }

    private Map<InterestCode, List<AskChatSampleQuestion>> groupByCategory(
            List<AskChatSampleQuestion> sampleQuestions
    ) {
        Map<InterestCode, List<AskChatSampleQuestion>> questionsByCategory =
                new EnumMap<>(InterestCode.class);

        for (AskChatSampleQuestion sampleQuestion : sampleQuestions) {
            questionsByCategory
                    .computeIfAbsent(sampleQuestion.getInterestCode(), ignored -> new ArrayList<>())
                    .add(sampleQuestion);
        }

        Comparator<AskChatSampleQuestion> questionOrder = Comparator
                .comparingInt(AskChatSampleQuestion::getDisplayOrder)
                .thenComparing(AskChatSampleQuestion::getQuestion);
        questionsByCategory.values().forEach(questions -> questions.sort(questionOrder));
        return questionsByCategory;
    }

    private List<InterestCode> orderedCategories(
            Long userId,
            Map<InterestCode, List<AskChatSampleQuestion>> questionsByCategory
    ) {
        List<InterestCode> categories = new ArrayList<>(questionsByCategory.keySet());
        categories.sort(Comparator
                .comparingLong((InterestCode category) ->
                        mix(userId ^ (CATEGORY_SALT * (category.ordinal() + 1L))))
                .thenComparing(InterestCode::name));
        return categories;
    }

    private int rotatedIndex(long base, long rotationSlot, int size) {
        int baseIndex = Math.floorMod(base, size);
        int slotOffset = Math.floorMod(rotationSlot, size);
        return (baseIndex + slotOffset) % size;
    }

    private long mix(long value) {
        value = (value ^ (value >>> 33)) * 0xff51afd7ed558ccdl;
        value = (value ^ (value >>> 33)) * 0xc4ceb9fe1a85ec53l;
        return value ^ (value >>> 33);
    }
}

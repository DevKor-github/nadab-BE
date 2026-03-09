package com.devkor.ifive.nadab.domain.dailyreport.application.helper;

import org.springframework.util.StringUtils;

/**
 * 답변에서 키워드 매칭 문장 추출 유틸리티
 */
public class MatchedSnippetExtractor {

    private static final int MIN_SNIPPET_LENGTH = 20;
    private static final int MAX_SNIPPET_LENGTH = 100;

    public static String extract(String answerContent, String keyword) {
        if (!StringUtils.hasText(answerContent)) {
            return null;
        }

        // 키워드가 없거나 답변에 없으면 → 첫 문장
        if (!StringUtils.hasText(keyword) || !answerContent.contains(keyword)) {
            return extractFirstSentence(answerContent);
        }

        // 답변에 키워드 있으면 → 키워드 포함 문장
        return extractSentenceWithKeyword(answerContent, keyword);
    }

    /**
     * 첫 문장 추출 (최소 20자 보장, 최대 100자 제한, 문장 단위)
     */
    private static String extractFirstSentence(String text) {
        String[] sentences = text.split("[.!?]\\s*");
        StringBuilder result = new StringBuilder();

        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (!trimmed.isEmpty()) {
                // 이 문장 추가 시 100자 초과 여부 체크
                int additionalLength = (result.length() > 0 ? 2 : 0) + trimmed.length();
                if (result.length() + additionalLength > MAX_SNIPPET_LENGTH) {
                    // 추가하면 100자 넘음 → 현재까지만 반환
                    break;
                }

                if (result.length() > 0) {
                    result.append(". ");
                }
                result.append(trimmed);

                // 최소 20자 보장
                if (result.length() >= MIN_SNIPPET_LENGTH) {
                    break;
                }
            }
        }

        // 문장이 없으면 원본 텍스트를 100자로 자름
        if (result.length() == 0) {
            return truncate(text.trim());
        }

        return result.toString();
    }

    /**
     * 키워드 포함 문장 추출 (최소 20자 보장, 최대 100자 제한, 문장 단위)
     */
    private static String extractSentenceWithKeyword(String text, String keyword) {
        String[] sentences = text.split("[.!?]\\s*");
        StringBuilder result = new StringBuilder();
        boolean keywordFound = false;
        int keywordIndex = -1;

        // 키워드 포함 문장 찾기
        for (int i = 0; i < sentences.length; i++) {
            if (sentences[i].contains(keyword)) {
                keywordFound = true;
                keywordIndex = i;
                break;
            }
        }

        if (!keywordFound) {
            // 키워드가 없으면 원본 텍스트를 100자로 자름 (문장 분리 실패 케이스)
            return truncate(text.trim());
        }

        // 키워드 포함 문장부터 시작하여 최소 20자 보장
        for (int i = keywordIndex; i < sentences.length; i++) {
            String trimmed = sentences[i].trim();
            if (!trimmed.isEmpty()) {
                // 이 문장 추가 시 100자 초과 여부 체크
                int additionalLength = (result.length() > 0 ? 2 : 0) + trimmed.length();
                if (result.length() + additionalLength > MAX_SNIPPET_LENGTH) {
                    // 추가하면 100자 넘음 → 현재까지만 반환
                    break;
                }

                if (result.length() > 0) {
                    result.append(". ");
                }
                result.append(trimmed);

                // 최소 20자 보장
                if (result.length() >= MIN_SNIPPET_LENGTH) {
                    break;
                }
            }
        }

        return result.toString();
    }

    private static String truncate(String text) {
        if (text.length() <= MAX_SNIPPET_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_SNIPPET_LENGTH) + "...";
    }
}
package com.devkor.ifive.nadab.domain.dailyreport.application.helper;

import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;

import java.time.LocalDate;

/**
 * Cursor 파싱 유틸리티
 * 형식: "date" (예: "2025-12-25")
 *
 * 하루에 하나의 답변만 가능하므로 date만으로 충분
 */
public class CursorParser {

    /**
     * Cursor 문자열 파싱
     *
     * @param cursor 커서 문자열 (null이면 null 반환)
     * @return 파싱된 날짜
     * @throws BadRequestException 잘못된 형식
     */
    public static LocalDate parse(String cursor) {
        if (cursor == null || cursor.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(cursor);
        } catch (Exception e) {
            throw new BadRequestException(ErrorCode.VALIDATION_FAILED);
        }
    }

    /**
     * Cursor 문자열 생성
     *
     * @param date 날짜
     * @return 인코딩된 커서 문자열
     */
    public static String encode(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.toString();
    }
}
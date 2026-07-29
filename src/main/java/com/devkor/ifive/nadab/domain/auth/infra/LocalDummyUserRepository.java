package com.devkor.ifive.nadab.domain.auth.infra;

import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Profile("local")
@Repository
@RequiredArgsConstructor
public class LocalDummyUserRepository {

    private static final long DUMMY_USER_ID = 11111L;
    private static final String DUMMY_EMAIL = "test@example.com";
    private static final String DUMMY_PASSWORD_HASH = "hashed_pw";
    private static final String DUMMY_NICKNAME = "TestUser";
    private static final long INITIAL_CRYSTAL_BALANCE = 1_000L;
    private static final int INITIAL_FREE_TURN_BALANCE = 3;
    private static final int INITIAL_PAID_TURN_BALANCE = 1_000;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Long createIfAbsent() {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("userId", DUMMY_USER_ID)
                .addValue("email", DUMMY_EMAIL)
                .addValue("passwordHash", DUMMY_PASSWORD_HASH)
                .addValue("nickname", DUMMY_NICKNAME)
                .addValue("interestCode", InterestCode.ROUTINE.name())
                .addValue("crystalBalance", INITIAL_CRYSTAL_BALANCE)
                .addValue("freeTurnBalance", INITIAL_FREE_TURN_BALANCE)
                .addValue("paidTurnBalance", INITIAL_PAID_TURN_BALANCE);

        jdbcTemplate.update("""
                INSERT INTO users (
                    id,
                    email,
                    password_hash,
                    nickname,
                    profile_image_key,
                    default_profile_type,
                    signup_status,
                    registered_at,
                    created_at,
                    updated_at
                )
                VALUES (
                    :userId,
                    :email,
                    :passwordHash,
                    :nickname,
                    NULL,
                    'DEFAULT',
                    'PROFILE_INCOMPLETE',
                    CURRENT_TIMESTAMP,
                    CURRENT_TIMESTAMP,
                    CURRENT_TIMESTAMP
                )
                ON CONFLICT (id) DO NOTHING
                """, parameters);

        jdbcTemplate.update("""
                INSERT INTO user_interests (user_id, interest_id)
                SELECT :userId, id
                  FROM interests
                 WHERE code = :interestCode
                ON CONFLICT (user_id) DO NOTHING
                """, parameters);

        jdbcTemplate.update("""
                INSERT INTO user_wallets (user_id, crystal_balance)
                VALUES (:userId, :crystalBalance)
                ON CONFLICT (user_id) DO NOTHING
                """, parameters);

        jdbcTemplate.update("""
                INSERT INTO ask_chat_wallets (
                    user_id,
                    free_turn_balance,
                    paid_turn_balance,
                    version,
                    created_at,
                    updated_at
                )
                VALUES (
                    :userId,
                    :freeTurnBalance,
                    :paidTurnBalance,
                    0,
                    CURRENT_TIMESTAMP,
                    CURRENT_TIMESTAMP
                )
                ON CONFLICT (user_id) DO NOTHING
                """, parameters);

        return DUMMY_USER_ID;
    }
}

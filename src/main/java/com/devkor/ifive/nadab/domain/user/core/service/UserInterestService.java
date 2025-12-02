package com.devkor.ifive.nadab.domain.user.core.service;

import com.devkor.ifive.nadab.domain.user.core.entity.Interest;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.entity.UserInterest;
import com.devkor.ifive.nadab.domain.user.core.repository.InterestRepository;
import com.devkor.ifive.nadab.domain.user.core.repository.UserInterestRepository;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserInterestService {

    private final InterestRepository interestRepository;
    private final UserInterestRepository userInterestRepository;

    /**
     * 유저의 관심 주제 업데이트 또는 생성(온보딩 시)
     */
    public void updateUserInterest(User user, InterestCode code) {
        Interest interest = interestRepository.findByCode(code)
                .orElseThrow(() -> new NotFoundException("유효하지 않은 관심 주제입니다: " + code));

        updateOrCreateUserInterest(user, interest);
    }

    private void updateOrCreateUserInterest(User user, Interest interest) {
        userInterestRepository.findByUser(user)
                .map(existing -> {
                    existing.updateInterest(interest);
                    return existing;
                })
                .orElseGet(() -> userInterestRepository.save(UserInterest.create(user, interest)));
    }
}

package com.devkor.ifive.nadab.domain.user.application;

import com.devkor.ifive.nadab.domain.user.api.dto.request.CreateProfileImageUploadUrlRequest;
import com.devkor.ifive.nadab.domain.user.api.dto.request.UpdateUserInterestRequest;
import com.devkor.ifive.nadab.domain.user.api.dto.request.UpdateUserProfileRequest;
import com.devkor.ifive.nadab.domain.user.api.dto.response.CreateProfileImageUploadUrlResponse;
import com.devkor.ifive.nadab.domain.user.api.dto.response.UpdateUserProfileResponse;
import com.devkor.ifive.nadab.domain.user.core.entity.InterestCode;
import com.devkor.ifive.nadab.domain.user.core.entity.SignupStatusType;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.user.core.service.ProfileImageService;
import com.devkor.ifive.nadab.domain.user.core.service.UserInterestService;
import com.devkor.ifive.nadab.domain.user.core.service.UserProfileUpdateService;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import com.devkor.ifive.nadab.global.core.response.ErrorCode;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final ProfileImageUrlBuilder profileImageUrlBuilder;
    private final ProfileImageService profileImageService;
    private final UserProfileUpdateService userProfileUpdateService;
    private final UserInterestService userInterestService;

    @Value("${profile-image.env}")
    private String env;

    public CreateProfileImageUploadUrlResponse createUploadUrl(
            Long userId,
            CreateProfileImageUploadUrlRequest request) {

        // content type / 확장자 검증
        String contentType = request.contentType();
        if (!"image/jpeg".equalsIgnoreCase(contentType)
                && !"image/png".equalsIgnoreCase(contentType)) {
            throw new BadRequestException(ErrorCode.IMAGE_UNSUPPORTED_TYPE);
        }

        String extension = switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            default -> throw new BadRequestException(ErrorCode.IMAGE_UNSUPPORTED_TYPE);
        };

        String uuid = UUID.randomUUID().toString();
        String objectKey = "%s/profiles/original/%d/%s.%s"
                .formatted(env, userId, uuid, extension);

        String uploadUrl = profileImageService.generatePresignedUploadUrl(objectKey, contentType);

        return new CreateProfileImageUploadUrlResponse(objectKey, uploadUrl);
    }

    public void deleteProfileImage(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        userProfileUpdateService.deleteProfileImage(user);
    }

    public UpdateUserProfileResponse updateUserProfile(Long id, UpdateUserProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        if (request.nickname() == null && request.objectKey() == null) {
            throw new BadRequestException(ErrorCode.USER_UPDATE_NO_DATA);
        }

        if (request.nickname() != null) {
            userProfileUpdateService.updateNickname(user, request.nickname());
        }
        if (request.objectKey() != null) {
            userProfileUpdateService.updateProfileImage(user, request.objectKey());
        }

        // 온보딩인 경우 완료 처리
        if (user.getSignupStatus().name().equals(SignupStatusType.PROFILE_INCOMPLETE.name())) {
            user.updateSignupStatus(SignupStatusType.COMPLETED);
        }

        return new UpdateUserProfileResponse(
                user.getNickname(),
                user.getEmail(),
                profileImageUrlBuilder.buildUserProfileUrl(user)
        );
    }

    public void updateUserInterest(Long userId, UpdateUserInterestRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        InterestCode code = InterestCode.fromString(request.interestCode());

        userInterestService.updateUserInterest(user, code);
    }
}

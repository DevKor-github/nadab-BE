package com.devkor.ifive.nadab.domain.user.application;

import com.devkor.ifive.nadab.domain.user.api.dto.request.CreateProfileImageUploadUrlRequest;
import com.devkor.ifive.nadab.domain.user.api.dto.request.UpdateUserProfileRequest;
import com.devkor.ifive.nadab.domain.user.api.dto.response.CreateProfileImageUploadUrlResponse;
import com.devkor.ifive.nadab.domain.user.api.dto.response.UpdateUserProfileResponse;
import com.devkor.ifive.nadab.domain.user.core.entity.DefaultProfileType;
import com.devkor.ifive.nadab.domain.user.core.entity.SignupStatusType;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import com.devkor.ifive.nadab.domain.user.core.repository.UserRepository;
import com.devkor.ifive.nadab.domain.user.core.service.ProfileImageService;
import com.devkor.ifive.nadab.domain.user.core.service.UserProfileUpdateService;
import com.devkor.ifive.nadab.domain.user.infra.ProfileImageUrlBuilder;
import com.devkor.ifive.nadab.global.exception.BadRequestException;
import com.devkor.ifive.nadab.global.exception.NotFoundException;
import jakarta.validation.Valid;
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

    @Value("${profile-image.env}")
    private String env;

    public CreateProfileImageUploadUrlResponse createUploadUrl(
            Long userId,
            CreateProfileImageUploadUrlRequest request) {

        // content type / 확장자 검증
        String contentType = request.contentType();
        if (!"image/jpeg".equalsIgnoreCase(contentType)
                && !"image/png".equalsIgnoreCase(contentType)) {
            throw new BadRequestException("지원하지 않는 이미지 형식입니다. (jpg, png만 허용)");
        }

        String extension = switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            default -> throw new BadRequestException("지원하지 않는 이미지 형식입니다.");
        };

        String uuid = UUID.randomUUID().toString();
        String objectKey = "%s/profiles/original/%d/%s.%s"
                .formatted(env, userId, uuid, extension);

        String uploadUrl = profileImageService.generatePresignedUploadUrl(objectKey, contentType);

        return new CreateProfileImageUploadUrlResponse(objectKey, uploadUrl);
    }

    public void deleteProfileImage(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. id: " + id));

        userProfileUpdateService.deleteProfileImage(user);
    }

    public UpdateUserProfileResponse updateUserProfile(Long id, @Valid UpdateUserProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. id: " + id));

        if (request.nickname() == null && request.objectKey() == null) {
            throw new BadRequestException("수정할 프로필 정보가 없습니다.");
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
}

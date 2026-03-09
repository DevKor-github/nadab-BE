package com.devkor.ifive.nadab.domain.user.infra;

import com.devkor.ifive.nadab.domain.user.core.entity.DefaultProfileType;
import com.devkor.ifive.nadab.domain.user.core.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProfileImageUrlBuilder {

    @Value("${profile-image.base-url}")
    private String baseUrl;

    public String buildUrl(String objectKey) {
        if (objectKey == null) {
            return null;
        }
        return baseUrl + "/" + objectKey;
    }

    public String buildDefaultUrl(DefaultProfileType type) {
        if (type == null) {
            return null;
        }
        return baseUrl + "/default/" + type.name() + ".png";
    }

    public String buildUserProfileUrl(User user) {
        if (user.getProfileImageKey() != null) {
            return buildUrl(user.getProfileImageKey());
        }

        if (user.getDefaultProfileType() != null) {
            return buildDefaultUrl(user.getDefaultProfileType());
        }

        return null;
    }

    public String buildAnalysisTypeImageUrl(String analysisTypeCode) {
        if (analysisTypeCode == null) {
            return null;
        }
        return baseUrl + "/resources/analysis_type/" + analysisTypeCode + ".png";
    }
}

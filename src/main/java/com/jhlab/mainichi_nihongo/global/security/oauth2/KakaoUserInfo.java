package com.jhlab.mainichi_nihongo.global.security.oauth2;

import com.jhlab.mainichi_nihongo.domain.user.type.Provider;

import java.util.Map;

public class KakaoUserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;
    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> profile;

    @SuppressWarnings("unchecked")
    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        this.profile = kakaoAccount != null ? (Map<String, Object>) kakaoAccount.get("profile") : null;
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public Provider getProvider() {
        return Provider.KAKAO;
    }

    @Override
    public String getEmail() {
        if (kakaoAccount != null && kakaoAccount.get("email") != null) {
            return (String) kakaoAccount.get("email");
        }
        // 이메일 권한이 없는 경우 placeholder 이메일 생성
        return "kakao_" + getProviderId() + "@kakao.local";
    }

    @Override
    public String getName() {
        return profile != null ? (String) profile.get("nickname") : null;
    }

    @Override
    public String getProfileImage() {
        return profile != null ? (String) profile.get("profile_image_url") : null;
    }
}

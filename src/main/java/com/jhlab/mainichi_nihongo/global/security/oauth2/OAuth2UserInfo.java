package com.jhlab.mainichi_nihongo.global.security.oauth2;

import com.jhlab.mainichi_nihongo.domain.user.type.Provider;

public interface OAuth2UserInfo {
    String getProviderId();

    Provider getProvider();

    String getEmail();

    String getName();

    String getProfileImage();
}

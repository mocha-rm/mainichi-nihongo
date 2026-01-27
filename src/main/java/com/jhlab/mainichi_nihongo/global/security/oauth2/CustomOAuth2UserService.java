package com.jhlab.mainichi_nihongo.global.security.oauth2;

import com.jhlab.mainichi_nihongo.domain.subscribe.repository.SubscriberRepository;
import com.jhlab.mainichi_nihongo.domain.user.entity.User;
import com.jhlab.mainichi_nihongo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final SubscriberRepository subscriberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2UserInfo userInfo = getOAuth2UserInfo(registrationId, attributes);
        User user = saveOrUpdate(userInfo);

        return new CustomOAuth2User(user, attributes);
    }

    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleUserInfo(attributes);
            case "kakao" -> new KakaoUserInfo(attributes);
            default -> throw new OAuth2AuthenticationException("Platforms not supported: " + registrationId);
        };
    }

    private User saveOrUpdate(OAuth2UserInfo userInfo) {
        Optional<User> existingUser =
                userRepository.findByProviderAndProviderId(userInfo.getProvider(), userInfo.getProviderId());

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.updateLastLogin();
            return user;
        }

        User newUser = User.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .profileImage(userInfo.getProfileImage())
                .provider(userInfo.getProvider())
                .providerId(userInfo.getProviderId())
                .build();

        if (userInfo.getEmail() != null) {
            subscriberRepository.findByEmail(userInfo.getEmail()).ifPresent(newUser::linkSubscriber);
        }

        log.info("Register new user: {} ({})", userInfo.getEmail(), userInfo.getProvider());

        return userRepository.save(newUser);
    }
}

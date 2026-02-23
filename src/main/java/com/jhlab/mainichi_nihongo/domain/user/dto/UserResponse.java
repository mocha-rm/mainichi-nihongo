package com.jhlab.mainichi_nihongo.domain.user.dto;

import com.jhlab.mainichi_nihongo.domain.user.entity.User;

public record UserResponse(
        Long id,
        String email,
        String name,
        String profileImage,
        String provider,
        String tier,
        boolean isSubscriber
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getProfileImage(),
                user.getProvider().name(),
                user.getTier().name(),
                user.getSubscriber() != null
        );
    }
}

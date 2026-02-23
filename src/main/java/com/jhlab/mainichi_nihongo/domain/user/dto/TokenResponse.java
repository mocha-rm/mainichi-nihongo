package com.jhlab.mainichi_nihongo.domain.user.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        Long expiresIn
) {}

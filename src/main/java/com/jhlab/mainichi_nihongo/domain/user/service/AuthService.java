package com.jhlab.mainichi_nihongo.domain.user.service;

import com.jhlab.mainichi_nihongo.domain.user.dto.TokenResponse;
import com.jhlab.mainichi_nihongo.domain.user.entity.User;
import com.jhlab.mainichi_nihongo.domain.user.repository.UserRepository;
import com.jhlab.mainichi_nihongo.global.security.jwt.JwtProperties;
import com.jhlab.mainichi_nihongo.global.security.jwt.JwtTokenProvider;
import com.jhlab.mainichi_nihongo.global.security.jwt.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;

    public TokenResponse refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 Refresh Token 입니다.");
        }

        Long userId = refreshTokenService.getUserIdByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh Token 이 만료되었거나 존재하지 않습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        refreshTokenService.deleteRefreshToken(refreshToken);
        refreshTokenService.saveRefreshToken(user.getId(), newRefreshToken);

        return new TokenResponse(
                newAccessToken,
                newRefreshToken,
                jwtProperties.getAccessTokenExpiry()
        );
    }

    public void logout(Long userId) {
        refreshTokenService.deleteByUserId(userId);
        log.info("user logout completed: userId={}", userId);
    }
}

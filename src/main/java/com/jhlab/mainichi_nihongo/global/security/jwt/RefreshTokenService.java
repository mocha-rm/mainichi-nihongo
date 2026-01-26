package com.jhlab.mainichi_nihongo.global.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String USER_TOKEN_PREFIX = "user_refresh:";

    /**
     * Refresh Token 을 Redis 에 저장
     * - refresh_token:{token} -> userId (토큰으로 사용자 조회)
     * - user_refresh:{userId} -> token (사용자의 토큰 관리)
     */
    public void saveRefreshToken(Long userId, String refreshToken) {
        long expirySeconds = jwtProperties.getRefreshTokenExpiry() / 1000;

        deleteByUserId(userId);

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + refreshToken,
                String.valueOf(userId),
                expirySeconds,
                TimeUnit.SECONDS
        );

        redisTemplate.opsForValue().set(
                USER_TOKEN_PREFIX + userId,
                refreshToken,
                expirySeconds,
                TimeUnit.SECONDS
        );

        log.debug("Refresh token saved for userId: {}", userId);
    }

    /**
     * Refresh Token 으로 사용자 ID 조회
     */
    public Optional<Long> getUserIdByRefreshToken(String refreshToken) {
        String userId = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + refreshToken);

        return userId != null ? Optional.of(Long.parseLong(userId)) : Optional.empty();
    }

    /**
     * Refresh Token 유효성 검증
     */
    public boolean validateRefreshToken(String refreshToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(REFRESH_TOKEN_PREFIX + refreshToken));
    }

    /**
     * 사용자 ID로 Refresh Token 삭제 (로그아웃)
     */
    public void deleteByUserId(Long userId) {
        String existingToken = redisTemplate.opsForValue().get(USER_TOKEN_PREFIX + userId);
        if (existingToken != null) {
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + existingToken);
            redisTemplate.delete(USER_TOKEN_PREFIX + userId);
            log.debug("Refresh token deleted for userId: {}", userId);
        }
    }

    /**
     * Refresh Token 삭제
     */
    public void deleteRefreshToken(String refreshToken) {
        String userId = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + refreshToken);

        if (userId != null) {
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + refreshToken);
            redisTemplate.delete(USER_TOKEN_PREFIX + userId);
        }
    }
}

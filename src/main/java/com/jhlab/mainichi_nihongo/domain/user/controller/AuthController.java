package com.jhlab.mainichi_nihongo.domain.user.controller;

import com.jhlab.mainichi_nihongo.domain.user.dto.TokenRefreshRequest;
import com.jhlab.mainichi_nihongo.domain.user.dto.TokenResponse;
import com.jhlab.mainichi_nihongo.domain.user.dto.UserResponse;
import com.jhlab.mainichi_nihongo.domain.user.entity.User;
import com.jhlab.mainichi_nihongo.domain.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody TokenRefreshRequest request) {
        TokenResponse response = authService.refreshAccessToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal User user) {
        authService.logout(user.getId());
        return ResponseEntity.ok().build();
    }
}

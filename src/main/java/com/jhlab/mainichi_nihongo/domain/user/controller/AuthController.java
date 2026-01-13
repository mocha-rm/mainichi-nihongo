package com.jhlab.mainichi_nihongo.domain.user.controller;

import com.jhlab.mainichi_nihongo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
}

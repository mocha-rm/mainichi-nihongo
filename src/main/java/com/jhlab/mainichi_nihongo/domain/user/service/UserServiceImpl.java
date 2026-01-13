package com.jhlab.mainichi_nihongo.domain.user.service;

import com.jhlab.mainichi_nihongo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
}

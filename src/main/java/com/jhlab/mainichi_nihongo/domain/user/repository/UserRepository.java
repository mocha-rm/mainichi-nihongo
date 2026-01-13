package com.jhlab.mainichi_nihongo.domain.user.repository;

import com.jhlab.mainichi_nihongo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}

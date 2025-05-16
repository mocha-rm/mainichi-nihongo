package com.jhlab.mainichi_nihongo.domain.subscriber.repository;

import com.jhlab.mainichi_nihongo.domain.subscriber.entity.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    boolean existsByEmail(String email);

    Optional<Subscriber> findByEmail(String email);
}

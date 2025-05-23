package com.jhlab.mainichi_nihongo.domain.subscribe.repository;

import com.jhlab.mainichi_nihongo.domain.subscribe.entity.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    boolean existsByEmail(String email);

    Optional<Subscriber> findByEmail(String email);

    @Query(value = "SELECT COUNT(1) FROM subscriber", nativeQuery = true)
    long getCountOfSubscribers();
}

package com.jhlab.mainichi_nihongo.domain.user.entity;

import com.jhlab.mainichi_nihongo.domain.subscribe.entity.Subscriber;
import com.jhlab.mainichi_nihongo.domain.user.type.SubscriptionTier;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.security.Provider;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String name;
    private String profileImage;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    private String providerId;

    @Enumerated(EnumType.STRING)
    private SubscriptionTier tier;

    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    // Subscriber 와 연동 (같은 이메일이면 자동 연결)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id")
    private Subscriber subscriber;
}

package com.jhlab.mainichi_nihongo.domain.user.entity;

import com.jhlab.mainichi_nihongo.domain.subscribe.entity.Subscriber;
import com.jhlab.mainichi_nihongo.domain.user.type.Provider;
import com.jhlab.mainichi_nihongo.domain.user.type.SubscriptionTier;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 100)
    private String name;

    @Column(length = 500)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(nullable = false)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionTier tier;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastLoginAt;

    // Subscriber 와 연동 (같은 이메일이면 자동 연결)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id")
    private Subscriber subscriber;

    @Builder
    public User(String email, String name, String profileImage, Provider provider, String providerId) {
        this.email = email;
        this.name = name;
        this.profileImage = profileImage;
        this.provider = provider;
        this.providerId = providerId;
        this.tier = SubscriptionTier.FREE;
        this.createdAt = LocalDateTime.now();
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void linkSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    public void upgradeToPremium() {
        this.tier = SubscriptionTier.PREMIUM;
    }

    public void downgradeToFree() {
        this.tier = SubscriptionTier.FREE;
    }

    public boolean isPremium() {
        return this.tier == SubscriptionTier.PREMIUM;
    }
}

package com.jhlab.mainichi_nihongo.domain.content.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 일본어 학습 콘텐츠의 테마를 관리하는 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentTheme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String JLPTLevel;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private boolean used;

    @Column
    private LocalDateTime usedAt;

    public ContentTheme(String JLPTLevel, String topic) {
        this.JLPTLevel = JLPTLevel;
        this.topic = topic;
    }

    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }
}

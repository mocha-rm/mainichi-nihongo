package com.jhlab.mainichi_nihongo.domain.email.entity;

import com.jhlab.mainichi_nihongo.domain.content.entity.ContentTheme;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 생성된 이메일 컨텐츠를 저장하는 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private ContentTheme theme;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean sent;

    @Column
    private LocalDateTime sentAt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String htmlContent;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String detailedContent;

    public EmailContent(ContentTheme theme, String htmlContent) {
        this.theme = theme;
        this.htmlContent = htmlContent;
        this.createdAt = LocalDateTime.now();
    }

    public void markAsSend() {
        this.sent = true;
        this.sentAt = LocalDateTime.now();
    }
}

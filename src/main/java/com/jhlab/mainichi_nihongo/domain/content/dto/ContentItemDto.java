package com.jhlab.mainichi_nihongo.domain.content.dto;

import com.jhlab.mainichi_nihongo.domain.email.entity.EmailContent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ContentItemDto {
    private final Long id;
    private final String title;
    private final String jlptLevel;
    private final String topic;
    private final LocalDateTime createdAt;

    public static ContentItemDto from(EmailContent emailContent) {
        String title = String.format("[%s] %s",
                emailContent.getTheme().getJLPTLevel(),
                emailContent.getTheme().getTopic());

        return new ContentItemDto(
                emailContent.getId(),
                title,
                emailContent.getTheme().getJLPTLevel(),
                emailContent.getTheme().getTopic(),
                emailContent.getCreatedAt()
        );
    }
}

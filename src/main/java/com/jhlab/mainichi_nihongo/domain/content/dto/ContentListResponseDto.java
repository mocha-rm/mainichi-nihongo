package com.jhlab.mainichi_nihongo.domain.content.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ContentListResponseDto {

    private final List<ContentItemDto> contents;
    private final int totalPages;
    private final long totalElements;
    private final int currentPage;
    private final boolean isFirst;
    private final boolean isLast;

    public static ContentListResponseDto from(Page<ContentItemDto> page) {
        return new ContentListResponseDto(
                page.getContent(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getNumber(),
                page.isFirst(),
                page.isLast()
        );
    }
}

package com.jhlab.mainichi_nihongo.domain.content.controller;

import com.jhlab.mainichi_nihongo.domain.content.dto.ContentListResponseDto;
import com.jhlab.mainichi_nihongo.domain.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contents")
public class ContentListController {
    private final ContentService contentService;

    @GetMapping("/list")
    public ResponseEntity<ContentListResponseDto> findAllContents(
            @PageableDefault(size = 8, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String jlptLevel,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false, defaultValue = "최신순") String sortOrder
    ) {
        ContentListResponseDto responseDto = contentService.findAllContents(pageable, jlptLevel, topic, sortOrder);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}

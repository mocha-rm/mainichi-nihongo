package com.jhlab.mainichi_nihongo.domain.content.controller;

import com.jhlab.mainichi_nihongo.domain.content.dto.ContentListResponseDto;
import com.jhlab.mainichi_nihongo.domain.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contents")
public class ContentListController {
    private final ContentService contentService;

    @GetMapping("/list")
    public ResponseEntity<ContentListResponseDto> findAllContents(@PageableDefault(size = 10) Pageable pageable) {
        ContentListResponseDto responseDto = contentService.findAllContents(pageable);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }
}

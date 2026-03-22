package com.jhlab.mainichi_nihongo.domain.content.controller;

import com.jhlab.mainichi_nihongo.domain.email.entity.EmailContent;
import com.jhlab.mainichi_nihongo.domain.email.repository.EmailContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

/**
 * 콘텐츠 이전글/다음글 네비게이션 API 컨트롤러
 */
@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
@Slf4j
public class ContentNavigationController {

    private final EmailContentRepository emailContentRepository;
    private static final DateTimeFormatter DATE_PARAM_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 이전 콘텐츠의 날짜를 반환합니다.
     * 해당 날짜 이전에 콘텐츠가 없으면 404를 반환합니다.
     */
    @GetMapping("/{date}/prev")
    public ResponseEntity<Map<String, String>> getPreviousContentDate(@PathVariable String date) {
        try {
            LocalDate targetDate = LocalDate.parse(date, DATE_PARAM_FORMAT);
            LocalDateTime targetDateTime = targetDate.atStartOfDay();

            Optional<EmailContent> previousContent = emailContentRepository.findPreviousContent(targetDateTime);

            if (previousContent.isPresent()) {
                String prevDate = previousContent.get().getCreatedAt()
                        .format(DATE_PARAM_FORMAT);
                log.info("이전 콘텐츠 날짜 조회 성공. 기준: {} → 이전: {}", date, prevDate);
                return ResponseEntity.ok(Map.of("date", prevDate));
            } else {
                log.info("이전 콘텐츠가 없습니다. 기준 날짜: {}", date);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("이전 콘텐츠 날짜 조회 중 오류: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 다음 콘텐츠의 날짜를 반환합니다.
     * 해당 날짜 이후에 콘텐츠가 없으면 404를 반환합니다.
     */
    @GetMapping("/{date}/next")
    public ResponseEntity<Map<String, String>> getNextContentDate(@PathVariable String date) {
        try {
            LocalDate targetDate = LocalDate.parse(date, DATE_PARAM_FORMAT);
            LocalDateTime targetDateTime = targetDate.atStartOfDay();

            Optional<EmailContent> nextContent = emailContentRepository.findNextContent(targetDateTime);

            if (nextContent.isPresent()) {
                String nextDate = nextContent.get().getCreatedAt()
                        .format(DATE_PARAM_FORMAT);
                log.info("다음 콘텐츠 날짜 조회 성공. 기준: {} → 다음: {}", date, nextDate);
                return ResponseEntity.ok(Map.of("date", nextDate));
            } else {
                log.info("다음 콘텐츠가 없습니다 (최신 게시글). 기준 날짜: {}", date);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("다음 콘텐츠 날짜 조회 중 오류: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}

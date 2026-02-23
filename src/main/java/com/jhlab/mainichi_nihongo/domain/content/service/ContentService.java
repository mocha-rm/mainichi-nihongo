package com.jhlab.mainichi_nihongo.domain.content.service;

import com.jhlab.mainichi_nihongo.domain.content.dto.ContentItemDto;
import com.jhlab.mainichi_nihongo.domain.content.dto.ContentListResponseDto;
import com.jhlab.mainichi_nihongo.domain.content.entity.ContentTheme;
import com.jhlab.mainichi_nihongo.domain.content.repository.ContentThemeRepository;
import com.jhlab.mainichi_nihongo.domain.email.entity.EmailContent;
import com.jhlab.mainichi_nihongo.domain.email.repository.EmailContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * 이메일에 포함될 일본어 학습 콘텐츠를 제공하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContentService {
    private final GeminiService geminiService;
    private final ContentThemeRepository contentThemeRepository;
    private final EmailContentRepository emailContentRepository;
    private final Random random = new Random();

    @Value("classpath:templates/email-template-new.html")
    private Resource emailTemplateResource;

    @Value("${app.unsubscribe.url:http://mainichi-nihongo.com/unsubscribe}")
    private String unsubscribeUrl;

    @Value("${app.server-url}")
    private String serverUrl;

    /**
     * 일본어 JLPT 레벨 목록
     */
    private static final List<String> JLPT_LEVELS = Arrays.asList("N5", "N4", "N3", "N2", "N1");

    /**
     * 일본어 학습 주제 목록
     */
    private static final List<String> TOPICS = Arrays.asList(
            "인사말", "자기소개", "가족", "취미", "음식", "여행",
            "쇼핑", "날씨", "건강", "업무 대화", "문법", "한자",
            "관용구", "속담", "축제와 명절", "일상 회화", "비즈니스 일본어"
    );

    /**
     * 오늘의 일본어 학습 콘텐츠를 생성
     */
    public String generateDailyContent() {
        Optional<EmailContent> todayContent = emailContentRepository.findByCreatedDate(LocalDateTime.now());

        if (todayContent.isPresent()) {
            log.info("오늘 이미 생성된 콘텐츠가 있습니다. ID: {}", todayContent.get().getId());
            return applyEmailTemplate(todayContent.get().getTheme());
        }

        ContentTheme theme = getOrCreateTheme();
        return saveContent(theme);
    }

    /**
     * 오늘의 일본어 학습 콘텐츠를 저장
     */
    @Transactional
    public String saveContent(ContentTheme theme) {
        String detailedContent = geminiService.generateContent(theme.getJLPTLevel(), theme.getTopic());

        EmailContent content = new EmailContent(theme, "");
        content.setDetailedContent(detailedContent);
        emailContentRepository.save(content);

        theme.markAsUsed();
        contentThemeRepository.save(theme);

        log.info("새로운 일본어 학습 콘텐츠가 생성되었습니다. 테마: {} {}", theme.getJLPTLevel(), theme.getTopic());
        return applyEmailTemplate(theme);
    }

    private ContentTheme getOrCreateTheme() {
        Optional<ContentTheme> unusedTheme = contentThemeRepository.findRandomUnusedTheme();

        if (unusedTheme.isPresent()) {
            return unusedTheme.get();
        }

        String JLPTLevel = JLPT_LEVELS.get(random.nextInt(JLPT_LEVELS.size()));
        String topic = TOPICS.get(random.nextInt(TOPICS.size()));

        ContentTheme newTheme = new ContentTheme(JLPTLevel, topic);
        return contentThemeRepository.save(newTheme);
    }

    private String applyEmailTemplate(ContentTheme theme) {
        try {
            String template = loadEmailTemplate();

            String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
            String urlDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            return template
                    .replace("${date}", formattedDate)
                    .replace("${level}", theme.getJLPTLevel())
                    .replace("${topic}", theme.getTopic())
                    .replace("${unsubscribeUrl}", unsubscribeUrl)
                    .replace("${serverUrl}", serverUrl)
                    .replace("${urlDate}", urlDate);
        } catch (IOException e) {
            log.error("이메일 템플릿 적용 중 오류 발생: {}", e.getMessage());
            return "이메일 템플릿 로드 중 오류가 발생했습니다.";
        }
    }

    private String loadEmailTemplate() throws IOException {
        try (Reader reader = new InputStreamReader(emailTemplateResource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }

    @Transactional
    public void initializeThemes() {
        if (contentThemeRepository.count() > 0) {
            log.info("테마 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }

        for (String level : JLPT_LEVELS) {
            for (String topic : TOPICS) {
                contentThemeRepository.save(new ContentTheme(level, topic));
            }
        }

        log.info("총 {}개의 테마가 초기화되었습니다.", JLPT_LEVELS.size() * TOPICS.size());
    }

    @Transactional(readOnly = true)
    public ContentListResponseDto findAllContents(
            Pageable pageable,
            String jlptLevel,
            String topic,
            String sortOrder
    ) {
        Sort.Direction direction = "등록일순".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(direction, "createdAt")
        );

        Page<EmailContent> contentPage = emailContentRepository.findAllWithFilters(
                jlptLevel,
                topic,
                sortedPageable
        );

        Page<ContentItemDto> dtoPage = contentPage.map(ContentItemDto::from);

        return ContentListResponseDto.from(dtoPage);
    }
}

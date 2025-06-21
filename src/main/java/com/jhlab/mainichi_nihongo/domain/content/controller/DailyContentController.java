package com.jhlab.mainichi_nihongo.domain.content.controller;

import com.jhlab.mainichi_nihongo.domain.content.service.ContentService;
import com.jhlab.mainichi_nihongo.domain.email.entity.EmailContent;
import com.jhlab.mainichi_nihongo.domain.email.repository.EmailContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Controller
@RequestMapping("/daily")
@RequiredArgsConstructor
@Slf4j
public class DailyContentController {
    
    private final EmailContentRepository emailContentRepository;
    private final ContentService contentService;

    @GetMapping
    public String showDailyContent(Model model) {
        try {
            Optional<EmailContent> todayContent = emailContentRepository.findByCreatedDate(LocalDateTime.now());
            
            if (todayContent.isPresent()) {
                EmailContent content = todayContent.get();
                String formattedDate = content.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));

                String detailedContent;
                if (content.getDetailedContent() != null && !content.getDetailedContent().isEmpty()) {
                    detailedContent = content.getDetailedContent();
                    log.info("저장된 상세 콘텐츠를 사용합니다. 길이: {}", detailedContent.length());
                } else {
                    log.warn("저장된 상세 콘텐츠가 없습니다. 기본 콘텐츠를 사용합니다.");
                    detailedContent = getDefaultFullContent();
                }

                String[] sections = parseContentSections(detailedContent);
                
                model.addAttribute("date", formattedDate);
                model.addAttribute("level", content.getTheme().getJLPTLevel());
                model.addAttribute("topic", content.getTheme().getTopic());
                model.addAttribute("wordsContent", sections[0]);
                model.addAttribute("conversationContent", sections[1]);
                model.addAttribute("cultureContent", sections[2]);
                model.addAttribute("dialectContent", sections[3]);
                
                log.info("오늘의 콘텐츠를 성공적으로 로드했습니다. 테마: {} {}", 
                    content.getTheme().getJLPTLevel(), content.getTheme().getTopic());
            } else {
                contentService.generateDailyContent();

                Optional<EmailContent> newContent = emailContentRepository.findByCreatedDate(LocalDateTime.now());
                if (newContent.isPresent()) {
                    EmailContent content = newContent.get();
                    String formattedDate = content.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));

                    String detailedContent;
                    if (content.getDetailedContent() != null && !content.getDetailedContent().isEmpty()) {
                        detailedContent = content.getDetailedContent();
                        log.info("새로 생성된 저장된 상세 콘텐츠를 사용합니다. 길이: {}", detailedContent.length());
                    } else {
                        log.warn("새로 생성된 콘텐츠에 상세 내용이 없습니다. 기본 콘텐츠를 사용합니다.");
                        detailedContent = getDefaultFullContent();
                    }
                    
                    String[] sections = parseContentSections(detailedContent);
                    
                    model.addAttribute("date", formattedDate);
                    model.addAttribute("level", content.getTheme().getJLPTLevel());
                    model.addAttribute("topic", content.getTheme().getTopic());
                    model.addAttribute("wordsContent", sections[0]);
                    model.addAttribute("conversationContent", sections[1]);
                    model.addAttribute("cultureContent", sections[2]);
                    model.addAttribute("dialectContent", sections[3]);
                    
                    log.info("새로운 콘텐츠를 생성하고 로드했습니다.");
                } else {
                    model.addAttribute("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));
                    model.addAttribute("level", "N3");
                    model.addAttribute("topic", "인사말");
                    model.addAttribute("wordsContent", getDefaultWordsContent());
                    model.addAttribute("conversationContent", getDefaultConversationContent());
                    model.addAttribute("cultureContent", getDefaultCultureContent());
                    model.addAttribute("dialectContent", getDefaultDialectContent());
                    
                    log.warn("콘텐츠 생성에 실패하여 기본 콘텐츠를 제공합니다.");
                }
            }
            
        } catch (Exception e) {
            log.error("오늘의 콘텐츠 로드 중 오류 발생: {}", e.getMessage());

            model.addAttribute("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));
            model.addAttribute("level", "N3");
            model.addAttribute("topic", "인사말");
            model.addAttribute("wordsContent", getDefaultWordsContent());
            model.addAttribute("conversationContent", getDefaultConversationContent());
            model.addAttribute("cultureContent", getDefaultCultureContent());
            model.addAttribute("dialectContent", getDefaultDialectContent());
        }
        
        return "daily-content";
    }
    
    private String[] parseContentSections(String content) {
        String[] sections = new String[4];

        content = content.replaceAll("href=\"/api/tts\\?text=([^\"]+)\"", "onclick=\"playTTS('$1')\" style=\"cursor: pointer;\"");
        content = content.replaceAll("<a[^>]*>🔊</a>", "<button class=\"tts-button\" onclick=\"playTTS(this.previousElementSibling.textContent.trim())\">🔊</button>");

        int wordsStart = content.indexOf("<!-- 핵심 단어 섹션 -->");
        int wordsEnd = content.indexOf("<!-- 실전 회화 섹션 -->");
        if (wordsStart != -1 && wordsEnd != -1) {
            sections[0] = content.substring(wordsStart + "<!-- 핵심 단어 섹션 -->".length(), wordsEnd).trim();
        } else {
            sections[0] = getDefaultWordsContent();
        }

        int conversationStart = content.indexOf("<!-- 실전 회화 섹션 -->");
        int conversationEnd = content.indexOf("<!-- 일본 문화 TMI 섹션 -->");
        if (conversationStart != -1 && conversationEnd != -1) {
            sections[1] = content.substring(conversationStart + "<!-- 실전 회화 섹션 -->".length(), conversationEnd).trim();
        } else {
            sections[1] = getDefaultConversationContent();
        }

        int cultureStart = content.indexOf("<!-- 일본 문화 TMI 섹션 -->");
        int cultureEnd = content.indexOf("<!-- 방언 탐방 섹션 -->");
        if (cultureStart != -1 && cultureEnd != -1) {
            sections[2] = content.substring(cultureStart + "<!-- 일본 문화 TMI 섹션 -->".length(), cultureEnd).trim();
        } else {
            sections[2] = getDefaultCultureContent();
        }

        int dialectStart = content.indexOf("<!-- 방언 탐방 섹션 -->");
        if (dialectStart != -1) {
            sections[3] = content.substring(dialectStart + "<!-- 방언 탐방 섹션 -->".length()).trim();
        } else {
            sections[3] = getDefaultDialectContent();
        }
        
        return sections;
    }
    
    private String getDefaultWordsContent() {
        return """
            <div class="word-card">
                <div class="japanese-text">一期一会 (いちごいちえ) 
                    <button class="tts-button" onclick="playTTS('一期一会')">🔊</button>
                </div>
                <div class="pronunciation">이치고이치에</div>
                <div class="meaning">일생에 한 번뿐인 만남</div>
                <div class="example">일본 다도(茶道)에서 유래한 말로, 모든 만남을 소중히 여기는 마음을 담고 있어요.</div>
            </div>
            <div class="word-card">
                <div class="japanese-text">木漏れ日 (こもれび) 
                    <button class="tts-button" onclick="playTTS('木漏れ日')">🔊</button>
                </div>
                <div class="pronunciation">코모레비</div>
                <div class="meaning">나뭇잎 사이로 비치는 햇살</div>
                <div class="example">일본의 아름다운 자연을 표현하는 대표적인 단어 중 하나예요.</div>
            </div>
            """;
    }
    
    private String getDefaultConversationContent() {
        return """
            <div class="conversation-item">
                <div class="conversation-situation">상황: 퇴근 시간, 동료와 헤어질 때</div>
                <div class="japanese-text">おつかれさま。また明日！ 
                    <button class="tts-button" onclick="playTTS('おつかれさま。また明日！')">🔊</button>
                </div>
                <div class="pronunciation">Otsukaresama. Mata ashita!</div>
                <div class="meaning">수고하셨어요. 내일 봐요!</div>
            </div>
            """;
    }
    
    private String getDefaultCultureContent() {
        return """
            <div class="culture-content">
                'おつかれさま'는 단순한 인사말이 아닌, 일본 직장 문화의 핵심을 보여주는 표현입니다. 
                상대방의 노력과 수고를 인정하고 존중하는 마음을 담고 있어요. 
                특히 일본에서는 퇴근할 때 이 말을 하지 않으면 예의가 없다고 여겨질 정도로 중요한 표현이랍니다.
            </div>
            """;
    }
    
    private String getDefaultDialectContent() {
        return """
            <div class="dialect-item">
                <div class="dialect-region">🏮 지역: 오사카</div>
                <div class="japanese-text">おつかれやん (Otsukarey-an) 
                    <button class="tts-button" onclick="playTTS('おつかれやん')">🔊</button>
                </div>
                <div class="pronunciation">표준어: おつかれさま (Otsukaresama)</div>
                <div class="meaning">수고하셨어요 (오사카식)</div>
            </div>
            """;
    }
    
    private String getDefaultFullContent() {
        return getDefaultWordsContent() + getDefaultConversationContent() + getDefaultCultureContent() + getDefaultDialectContent();
    }
} 
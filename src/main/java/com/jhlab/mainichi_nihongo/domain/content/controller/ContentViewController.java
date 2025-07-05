package com.jhlab.mainichi_nihongo.domain.content.controller;

import com.jhlab.mainichi_nihongo.domain.email.entity.EmailContent;
import com.jhlab.mainichi_nihongo.domain.email.repository.EmailContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 날짜별 콘텐츠를 보여주는 전용 컨트롤러
 */
@Controller
@RequestMapping("/contents")
@RequiredArgsConstructor
@Slf4j
public class ContentViewController {
    
    private final EmailContentRepository emailContentRepository;

    @GetMapping("/{date}")
    public String showContentByDate(@PathVariable String date, Model model) {
        try {
            LocalDate targetDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));

            ZonedDateTime startOfDay = targetDate.atStartOfDay(ZoneOffset.UTC);
            ZonedDateTime endOfDay = targetDate.atTime(23, 59, 59).atZone(ZoneOffset.UTC);

            List<EmailContent> content = emailContentRepository.findByCreatedDateBetween(
                    startOfDay.toLocalDateTime(),
                    endOfDay.toLocalDateTime()
            );
            
            if (!content.isEmpty()) {
                EmailContent emailContent = content.get(0);
                String formattedDate = emailContent.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));

                String detailedContent;
                if (emailContent.getDetailedContent() != null && !emailContent.getDetailedContent().isEmpty()) {
                    detailedContent = emailContent.getDetailedContent();
                    log.info("저장된 상세 콘텐츠를 사용합니다. 길이: {}", detailedContent.length());
                } else {
                    log.warn("저장된 상세 콘텐츠가 없습니다. 기본 콘텐츠를 사용합니다.");
                    detailedContent = getDefaultContent();
                }
                
                model.addAttribute("date", formattedDate);
                model.addAttribute("level", emailContent.getTheme().getJLPTLevel());
                model.addAttribute("topic", emailContent.getTheme().getTopic());
                model.addAttribute("content", detailedContent);
                
                log.info("콘텐츠를 성공적으로 로드했습니다. 날짜: {}, 테마: {} {}", 
                    date, emailContent.getTheme().getJLPTLevel(), emailContent.getTheme().getTopic());
            } else {
                String formattedDate = targetDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
                String defaultContent = getDefaultContent();
                
                model.addAttribute("date", formattedDate);
                model.addAttribute("level", "N3");
                model.addAttribute("topic", "일상 회화");
                model.addAttribute("content", defaultContent);
                
                log.warn("해당 날짜의 콘텐츠가 없어 기본 콘텐츠를 제공합니다. 날짜: {}", date);
            }
            
        } catch (Exception e) {
            log.error("콘텐츠 로드 중 오류 발생: {}", e.getMessage());

            model.addAttribute("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));
            model.addAttribute("level", "N3");
            model.addAttribute("topic", "일상 회화");
            model.addAttribute("content", getDefaultContent());
        }
        
        return "content-view";
    }
    
    private String getDefaultContent() {
        return """
            <div style="font-family: 'Noto Sans KR', Arial, sans-serif; line-height: 1.7; color: #333;">
                <!-- 오늘의 핵심 단어들 -->
                <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 20px; border-radius: 12px; margin-bottom: 25px; text-align: center;">
                    <h2 style="margin: 0 0 10px 0; font-size: 22px;">🎌 오늘의 일본어 보물상자</h2>
                    <p style="margin: 0; opacity: 0.9; font-size: 14px;">매일 새로운 표현으로 일본어 실력 업그레이드!</p>
                </div>
            
                <!-- 핵심 단어 5개 섹션 -->
                <div style="background: #fff8f3; border: 2px solid #ffa726; border-radius: 12px; padding: 20px; margin-bottom: 25px;">
                    <h3 style="color: #ff6b35; margin: 0 0 15px 0; display: flex; align-items: center;">
                        <span style="background: #ff6b35; color: white; border-radius: 50%%; width: 25px; height: 25px; display: inline-flex; align-items: center; justify-content: center; margin-right: 10px; font-size: 14px;">📝</span>
                        오늘의 핵심 단어 5선
                    </h3>
                    <div style="display: grid; gap: 12px;">
                        <div style="background: white; padding: 15px; border-radius: 8px; border-left: 4px solid #ffa726;">
                            <p style="margin: 0 0 5px 0; font-size: 16px;"><strong>일본어:</strong> <span class="japanese-text">一期一会 (いちごいちえ) <button class="tts-button" onclick="playTTS('一期一会')">🔊</button></span></p>
                            <p style="margin: 0 0 5px 0; color: #666;"><strong>발음:</strong> 이치고이치에</p>
                            <p style="margin: 0; color: #333;"><strong>의미:</strong> 일생에 한 번뿐인 만남</p>
                        </div>
                        <div style="background: white; padding: 15px; border-radius: 8px; border-left: 4px solid #ffa726;">
                            <p style="margin: 0 0 5px 0; font-size: 16px;"><strong>일본어:</strong> <span class="japanese-text">木漏れ日 (こもれび) <button class="tts-button" onclick="playTTS('木漏れ日')">🔊</button></span></p>
                            <p style="margin: 0 0 5px 0; color: #666;"><strong>발음:</strong> 코모레비</p>
                            <p style="margin: 0; color: #333;"><strong>의미:</strong> 나뭇잎 사이로 비치는 햇살</p>
                        </div>
                    </div>
                </div>
            
                <!-- 실전 회화 섹션 -->
                <div style="background: #f0f8ff; border: 2px solid #4a90e2; border-radius: 12px; padding: 20px; margin-bottom: 25px;">
                    <h3 style="color: #4a90e2; margin: 0 0 15px 0; display: flex; align-items: center;">
                        <span style="background: #4a90e2; color: white; border-radius: 50%%; width: 25px; height: 25px; display: inline-flex; align-items: center; justify-content: center; margin-right: 10px; font-size: 14px;">💬</span>
                        바로 써먹는 실전 회화
                    </h3>
                    <div style="display: grid; gap: 15px;">
                        <div style="background: white; padding: 15px; border-radius: 8px; margin-bottom: 10px; border-left: 4px solid #4a90e2;">
                            <p style="margin: 0 0 5px 0; font-size: 16px; color: #333;"><strong>상황:</strong> 퇴근 시간, 동료와 헤어질 때</p>
                            <p style="margin: 0 0 8px 0; font-size: 18px; color: #2c3e50;"><strong>일본어:</strong> <span class="japanese-text">おつかれさま。また明日！ <button class="tts-button" onclick="playTTS('おつかれさま。また明日！')">🔊</button></span></p>
                            <p style="margin: 0 0 5px 0; font-style: italic; color: #7f8c8d;"><strong>발음:</strong> Otsukaresama. Mata ashita!</p>
                            <p style="margin: 0; color: #e74c3c; font-weight: 500;"><strong>한국어:</strong> 수고하셨어요. 내일 봐요!</p>
                        </div>
                    </div>
                </div>
            
                <!-- 일본 문화 TMI 섹션 -->
                <div style="background: #f0fff4; border: 2px solid #52c41a; border-radius: 12px; padding: 20px; margin-bottom: 25px;">
                    <h3 style="color: #52c41a; margin: 0 0 15px 0; display: flex; align-items: center;">
                        <span style="background: #52c41a; color: white; border-radius: 50%%; width: 25px; height: 25px; display: inline-flex; align-items: center; justify-content: center; margin-right: 10px; font-size: 14px;">🎭</span>
                        오늘의 일본 문화 TMI
                    </h3>
                    <div style="background: white; padding: 15px; border-radius: 8px; border-left: 4px solid #52c41a;">
                        <p style="margin: 0; line-height: 1.6; color: #333;">'おつかれさま'는 단순한 인사말이 아닌, 일본 직장 문화의 핵심을 보여주는 표현입니다. 상대방의 노력과 수고를 인정하고 존중하는 마음을 담고 있어요.</p>
                    </div>
                </div>
            
                <!-- 방언 탐방 섹션 -->
                <div style="background: #fff0f6; border: 2px solid #eb2f96; border-radius: 12px; padding: 20px;">
                    <h3 style="color: #eb2f96; margin: 0 0 15px 0; display: flex; align-items: center;">
                        <span style="background: #eb2f96; color: white; border-radius: 50%%; width: 25px; height: 25px; display: inline-flex; align-items: center; justify-content: center; margin-right: 10px; font-size: 14px;">🗾</span>
                        오늘의 방언 탐방
                    </h3>
                    <div style="display: grid; gap: 15px;">
                        <div style="background: white; padding: 15px; border-radius: 8px; border-left: 4px solid #eb2f96;">
                            <p style="margin: 0 0 8px 0; color: #eb2f96; font-weight: 600;">🏮 지역: 오사카</p>
                            <p style="margin: 0 0 5px 0; font-size: 16px;"><strong>방언:</strong> <span class="japanese-text">おつかれやん (Otsukarey-an) <button class="tts-button" onclick="playTTS('おつかれやん')">🔊</button></span></p>
                            <p style="margin: 0 0 5px 0; color: #666;"><strong>표준어:</strong> おつかれさま (Otsukaresama)</p>
                            <p style="margin: 0; color: #333;"><strong>의미:</strong> 수고하셨어요 (오사카식)</p>
                        </div>
                    </div>
                </div>
            </div>
            """;
    }
} 
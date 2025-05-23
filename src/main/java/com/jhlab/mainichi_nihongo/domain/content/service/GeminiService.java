package com.jhlab.mainichi_nihongo.domain.content.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {
    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String generateContent(String level, String topic) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String prompt = createPrompt(level, topic);

            Map<String, Object> contents = Map.of(
                    "parts", List.of(Map.of("text", prompt))
            );
            Map<String, Object> request = Map.of(
                    "contents", List.of(contents)
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            String urlWithKey = apiUrl + "?key=" + apiKey;

            ResponseEntity<String> response = restTemplate.postForEntity(urlWithKey, entity, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            String rawContent = jsonNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            // 코드 블록 마커 제거
            return cleanupCodeBlockMarkers(rawContent);
        } catch (Exception e) {
            log.error("Gemini 콘텐츠 생성 중 오류: {}", e.getMessage());
            return "<p>기본 콘텐츠로 대체합니다.</p>";
        }
    }

    /**
     * Gemini 응답에서 코드 블록 마커(```html, ``` 등)를 제거하는 메서드
     */
    private String cleanupCodeBlockMarkers(String content) {
        if (content == null) {
            return "";
        }

        content = content.replaceAll("(?s)^```html\\s*", "");

        content = content.replaceAll("(?s)^```[a-zA-Z]*\\s*", "");

        content = content.replaceAll("(?s)\\s*```\\s*$", "");

        content = content.replaceAll("(?m)^```\\s*$", "");

        content = content.replaceAll("```", "");

        return content.trim();
    }

    private String createPrompt(String level, String topic) {
        return String.format("""
                당신은 '마이니치 니홍고'의 콘텐츠 크리에이터입니다! 구독자들이 매일 기대하며 열어보는 재미있고 유익한 일본어 콘텐츠를 만들어주세요.
                
                📋 오늘의 미션:
                - JLPT 레벨: %s
                - 주제: %s
                
                🎯 콘텐츠 구성 (다음 HTML 구조를 정확히 따라주세요):
                
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
                            <!-- 각 단어를 카드 형태로 배치 -->
                            <div style="background: white; padding: 15px; border-radius: 8px; border-left: 4px solid #ff6b35; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                                <strong style="color: #333; font-size: 18px;">단어1</strong>
                                <br><span style="color: #666; font-size: 14px;">발음</span>
                                <br><span style="color: #ff6b35; font-weight: 500;">의미</span>
                            </div>
                            <!-- 나머지 4개 단어도 같은 형식으로... -->
                        </div>
                    </div>
                
                    <!-- 실전 예문 섹션 -->
                    <div style="background: #f0f8ff; border: 2px solid #4a90e2; border-radius: 12px; padding: 20px; margin-bottom: 25px;">
                        <h3 style="color: #4a90e2; margin: 0 0 15px 0; display: flex; align-items: center;">
                            <span style="background: #4a90e2; color: white; border-radius: 50%%; width: 25px; height: 25px; display: inline-flex; align-items: center; justify-content: center; margin-right: 10px; font-size: 14px;">💬</span>
                            바로 써먹는 실전 회화
                        </h3>
                        <div style="background: white; padding: 15px; border-radius: 8px; margin-bottom: 10px; border-left: 4px solid #4a90e2;">
                            <p style="margin: 0 0 5px 0; font-size: 16px; color: #333;"><strong>상황:</strong> 구체적인 상황 설명</p>
                            <p style="margin: 0 0 8px 0; font-size: 18px; color: #2c3e50;"><strong>일본어:</strong> 예문</p>
                            <p style="margin: 0 0 5px 0; font-style: italic; color: #7f8c8d;"><strong>발음:</strong> 로마자</p>
                            <p style="margin: 0; color: #e74c3c; font-weight: 500;"><strong>한국어:</strong> 번역</p>
                        </div>
                        <!-- 추가 예문들... -->
                    </div>
                
                    <!-- 일본 문화 TMI 섹션 -->
                    <div style="background: #f0fff4; border: 2px solid #52c41a; border-radius: 12px; padding: 20px; margin-bottom: 25px;">
                        <h3 style="color: #52c41a; margin: 0 0 15px 0; display: flex; align-items: center;">
                            <span style="background: #52c41a; color: white; border-radius: 50%%; width: 25px; height: 25px; display: inline-flex; align-items: center; justify-content: center; margin-right: 10px; font-size: 14px;">🎭</span>
                            오늘의 일본 문화 TMI
                        </h3>
                        <div style="background: white; padding: 15px; border-radius: 8px; border-left: 4px solid #52c41a;">
                            <p style="margin: 0; line-height: 1.6; color: #333;">흥미로운 일본 문화나 상식 내용</p>
                        </div>
                    </div>
                
                    <!-- 방언 탐방 섹션 -->
                    <div style="background: #fff0f6; border: 2px solid #eb2f96; border-radius: 12px; padding: 20px; margin-bottom: 20px;">
                        <h3 style="color: #eb2f96; margin: 0 0 15px 0; display: flex; align-items: center;">
                            <span style="background: #eb2f96; color: white; border-radius: 50%%; width: 25px; height: 25px; display: inline-flex; align-items: center; justify-content: center; margin-right: 10px; font-size: 14px;">🗾</span>
                            오늘의 방언 탐방 (오사카/후쿠오카/홋카이도 등)
                        </h3>
                        <div style="background: white; padding: 15px; border-radius: 8px; border-left: 4px solid #eb2f96;">
                            <p style="margin: 0 0 8px 0; color: #eb2f96; font-weight: 600;">🏮 지역: 구체적 지역명</p>
                            <p style="margin: 0 0 5px 0; font-size: 16px;"><strong>방언:</strong> 방언 표현</p>
                            <p style="margin: 0 0 5px 0; color: #666;"><strong>표준어:</strong> 표준 일본어</p>
                            <p style="margin: 0; color: #333;"><strong>의미:</strong> 한국어 의미</p>
                        </div>
                    </div>
                
                    <!-- 오늘의 격려 메시지 -->
                    <div style="background: linear-gradient(45deg, #ff9a9e 0%%, #fecfef 100%%); padding: 15px; border-radius: 10px; text-align: center; margin-top: 20px;">
                        <p style="margin: 0; color: #8e0e3e; font-weight: 500; font-size: 15px;">
                            🌟 "작은 발걸음도 모이면 큰 여행이 됩니다!" - 오늘도 화이팅! 🌟
                        </p>
                    </div>
                </div>
                
                🎨 작성 가이드라인:
                - 친근하고 재미있는 톤앤매너 사용
                - 실제 일본에서 자주 쓰이는 실용적인 표현 위주
                - 각 섹션마다 구체적이고 흥미로운 내용 포함
                - 방언은 실제 지역 특색이 드러나는 것으로 선택
                - 문화 TMI는 일본인도 모를 수 있는 재미있는 사실
                - 모든 일본어에 정확한 후리가나와 로마자 표기
                - 절대로 ```html 같은 코드 블록 마커 사용 금지!
                
                💡 참고: 구독자들이 "오늘은 뭐 배울까?" 하며 설레며 열어보는 콘텐츠를 만들어주세요!
                """, level, topic);
    }
}

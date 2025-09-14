package com.jhlab.mainichi_nihongo.domain.content.controller;

import com.jhlab.mainichi_nihongo.domain.content.service.TTSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TTSController {
    private final TTSService textToSpeechService;

    @GetMapping("/tts")
    public ResponseEntity<String> getTTS(@RequestParam String text,
                                         @RequestParam(defaultValue = "2") int speakerId) {
        try {
            String cleanText = textToSpeechService.previewCleanText(text);
            log.info("TTS 요청 - 원본: '{}', 정리후: '{}'", text, cleanText);

            if (cleanText.isEmpty()) {
                return ResponseEntity.badRequest()
                        .contentType(MediaType.TEXT_HTML)
                        .body(createErrorHtml("텍스트에서 일본어를 찾을 수 없습니다: " + text));
            }

            // 실제 TTS 처리가 되는지 미리 확인하는 로직은 그대로 유지해도 좋습니다.
            textToSpeechService.synthesizeTextWithSpeaker(cleanText, speakerId);

            // audioUrl 생성 시 cleanText를 URL 인코딩합니다.
            String encodedText = UriUtils.encode(cleanText, StandardCharsets.UTF_8);
            String audioUrl = String.format("/api/tts/audio?text=%s&speakerId=%d", encodedText, speakerId);

            String html = createTTSHtml(cleanText, speakerId, audioUrl, text);

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html);

        } catch (Exception e) {
            log.error("TTS 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_HTML)
                    .body(createErrorHtml("TTS 처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/tts/audio")
    public ResponseEntity<Resource> getTTSAudio(@RequestParam String text,
                                                @RequestParam(defaultValue = "2") int speakerId) {
        log.info("### getTTSAudio에 수신된 text 파라미터 원본: '{}'", text);

        try {
            log.info("TTS 오디오 요청 - 텍스트: '{}', 화자ID: {}", text, speakerId);

            Resource audioResource = textToSpeechService.synthesizeTextWithSpeaker(text, speakerId);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/wav"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"tts.wav\"")
                    .body(audioResource);

        } catch (Exception e) {
            log.error("TTS 오디오 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/tts/speakers")
    public ResponseEntity<String> getAvailableSpeakers() {
        try {
            String speakers = textToSpeechService.getAvailableSpeakers();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(speakers);
        } catch (Exception e) {
            log.error("화자 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"화자 목록 조회 실패: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/tts/health")
    public ResponseEntity<String> checkVoicevoxHealth() {
        boolean isAvailable = textToSpeechService.isVoicevoxAvailable();
        if (isAvailable) {
            return ResponseEntity.ok("VOICEVOX is running");
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("VOICEVOX is not available");
        }
    }

    @GetMapping("/tts/preview")
    public ResponseEntity<String> previewCleanText(@RequestParam String text) {
        String cleanText = textToSpeechService.previewCleanText(text);
        String response = String.format(
                "{\"original\":\"%s\",\"cleaned\":\"%s\",\"isEmpty\":%b}",
                text.replace("\"", "\\\""),
                cleanText.replace("\"", "\\\""),
                cleanText.isEmpty()
        );
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    private String createTTSHtml(String cleanText, int speakerId, String audioUrl, String originalText) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>일본어 발음 듣기 - VOICEVOX</title>
                <style>
                    body {
                        font-family: 'Noto Sans KR', 'Noto Sans JP', Arial, sans-serif;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        min-height: 100vh;
                        margin: 0;
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                    }
                    .container {
                        background: rgba(255, 255, 255, 0.1);
                        padding: 2rem;
                        border-radius: 1rem;
                        backdrop-filter: blur(10px);
                        text-align: center;
                        max-width: 90%%;
                        width: 400px;
                        box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
                    }
                    h1 { margin: 0 0 1rem 0; font-size: 1.5rem; }
                    .text-info { margin: 1rem 0; padding: 1rem; background: rgba(255,255,255,0.1); border-radius: 0.5rem; border-left: 4px solid #00ff88; }
                    .original-text { font-size: 0.9rem; color: #e0e0e0; margin-bottom: 0.5rem; opacity: 0.8; }
                    .clean-text { font-size: 1.3rem; color: #fff; font-weight: bold; letter-spacing: 0.05em; }
                    audio { width: 100%%; margin: 1rem 0; border-radius: 0.5rem; }
                    .speaker-info { font-size: 0.9rem; color: #e0e0e0; margin-bottom: 1rem; background: rgba(0,0,0,0.2); padding: 0.5rem; border-radius: 0.3rem; }
                    .close-btn { background: rgba(255,255,255,0.2); border: none; color: white; padding: 0.5rem 1rem; border-radius: 0.5rem; cursor: pointer; margin-top: 1rem; transition: all 0.3s; font-size: 0.9rem; }
                    .close-btn:hover { background: rgba(255,255,255,0.3); transform: translateY(-1px); }
                    .status { font-size: 0.8rem; color: #00ff88; margin-top: 0.5rem; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🎌 일본어 발음 듣기</h1>
                    <div class="speaker-info">화자 ID: %d | VOICEVOX</div>
                    <div class="text-info">
                        <div class="original-text">원본: %s</div>
                        <div class="clean-text">%s</div>
                        <div class="status">✅ 텍스트 정리 완료</div>
                    </div>
                    <audio controls autoplay>
                        <source src="%s" type="audio/wav">
                        브라우저가 오디오 재생을 지원하지 않습니다.
                    </audio>
                    <button class="close-btn" onclick="window.close()">창 닫기</button>
                </div>
            </body>
            </html>
            """, speakerId,
                originalText.replace("<", "&lt;").replace(">", "&gt;"),
                cleanText.replace("<", "&lt;").replace(">", "&gt;"),
                audioUrl);
    }

    private String createErrorHtml(String errorMessage) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>TTS 오류</title>
                <style>
                    body {
                        font-family: 'Noto Sans KR', Arial, sans-serif;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        min-height: 100vh;
                        margin: 0;
                        background: linear-gradient(135deg, #ff6b6b 0%%, #ee5a52 100%%);
                        color: white;
                    }
                    .container {
                        background: rgba(255, 255, 255, 0.1);
                        padding: 2rem;
                        border-radius: 1rem;
                        backdrop-filter: blur(10px);
                        text-align: center;
                        max-width: 90%%;
                        width: 400px;
                    }
                    .error-icon { font-size: 3rem; margin-bottom: 1rem; }
                    .error-message { font-size: 1.1rem; margin-bottom: 1.5rem; }
                    .close-btn { background: rgba(255, 255, 255, 0.2); border: none; color: white; padding: 0.5rem 1rem; border-radius: 0.5rem; cursor: pointer; transition: background 0.3s; }
                    .close-btn:hover { background: rgba(255, 255, 255, 0.3); }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="error-icon">⚠️</div>
                    <h1>TTS 처리 오류</h1>
                    <div class="error-message">%s</div>
                    <button class="close-btn" onclick="window.close()">창 닫기</button>
                </div>
            </body>
            </html>
            """, errorMessage.replace("<", "&lt;").replace(">", "&gt;"));
    }
}
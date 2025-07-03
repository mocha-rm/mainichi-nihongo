package com.jhlab.mainichi_nihongo.domain.content.controller;

import com.jhlab.mainichi_nihongo.domain.content.service.TTSService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TTSController {
    private final TTSService textToSpeechService;

    @GetMapping("/tts")
    public ResponseEntity<String> getTTS(@RequestParam String text) throws IOException {
        Resource audioResource = textToSpeechService.synthesizeText(text);
        String audioUrl = "/api/tts/audio?text=" + text;
        
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>일본어 발음 듣기</title>
                <style>
                    body {
                        font-family: 'Noto Sans KR', Arial, sans-serif;
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
                    }
                    h1 {
                        margin: 0 0 1rem 0;
                        font-size: 1.5rem;
                    }
                    .text {
                        margin: 1rem 0;
                        font-size: 1.2rem;
                        color: #fff;
                    }
                    audio {
                        width: 100%%;
                        margin: 1rem 0;
                    }
                    .close-btn {
                        background: rgba(255, 255, 255, 0.2);
                        border: none;
                        color: white;
                        padding: 0.5rem 1rem;
                        border-radius: 0.5rem;
                        cursor: pointer;
                        margin-top: 1rem;
                        transition: background 0.3s;
                    }
                    .close-btn:hover {
                        background: rgba(255, 255, 255, 0.3);
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🎌 일본어 발음 듣기</h1>
                    <div class="text">%s</div>
                    <audio controls autoplay>
                        <source src="%s" type="audio/mpeg">
                        브라우저가 오디오 재생을 지원하지 않습니다.
                    </audio>
                    <button class="close-btn" onclick="window.close()">창 닫기</button>
                </div>
            </body>
            </html>
            """.formatted(text, audioUrl);

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    @GetMapping("/tts/audio")
    public ResponseEntity<Resource> getTTSAudio(@RequestParam String text) throws IOException {
        Resource audioResource = textToSpeechService.synthesizeText(text);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(audioResource);
    }
} 
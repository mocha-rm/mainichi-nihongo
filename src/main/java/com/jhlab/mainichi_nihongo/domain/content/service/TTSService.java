package com.jhlab.mainichi_nihongo.domain.content.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TTSService {

    @Value("${voicevox.base-url:http://localhost:50021}")
    private String voicevoxBaseUrl;

    @Value("${voicevox.speaker-id:2}")
    private int defaultSpeakerId;

    private final RestTemplate restTemplate;

    public Resource synthesizeText(String text) throws IOException {
        try {
            String cleanText = cleanTextForTTS(text);
            if (cleanText.isEmpty()) {
                throw new IllegalArgumentException("TTS를 위한 텍스트가 비어있습니다. 원본: " + text);
            }

            URI audioQueryUri = UriComponentsBuilder.fromUriString(voicevoxBaseUrl + "/audio_query")
                    .queryParam("speaker", defaultSpeakerId)
                    .queryParam("text", cleanText)
                    .build()
                    .encode()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> queryResponse = restTemplate.postForEntity(audioQueryUri, entity, Map.class);

            if (!queryResponse.getStatusCode().is2xxSuccessful() || queryResponse.getBody() == null) {
                throw new IOException("VOICEVOX audio_query 실패: " + queryResponse.getStatusCode() + " - " + queryResponse.getBody());
            }

            String synthesisUrl = voicevoxBaseUrl + "/synthesis?speaker=" + defaultSpeakerId;
            HttpHeaders synthesisHeaders = new HttpHeaders();
            synthesisHeaders.setContentType(MediaType.APPLICATION_JSON);
            synthesisHeaders.setAccept(List.of(MediaType.parseMediaType("audio/wav")));

            HttpEntity<Map> synthesisRequest = new HttpEntity<>(queryResponse.getBody(), synthesisHeaders);
            ResponseEntity<byte[]> audioResponse = restTemplate.exchange(
                    synthesisUrl, HttpMethod.POST, synthesisRequest, byte[].class
            );

            if (!audioResponse.getStatusCode().is2xxSuccessful() || audioResponse.getBody() == null) {
                throw new IOException("VOICEVOX synthesis 실패: " + audioResponse.getStatusCode());
            }

            return new ByteArrayResource(audioResponse.getBody()) {
                @Override
                public String getFilename() {
                    return "tts.wav";
                }
            };

        } catch (Exception e) {
            throw new IOException("VOICEVOX TTS 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }

    public Resource synthesizeTextWithSpeaker(String text, int speakerId) throws IOException {
        int originalSpeakerId = this.defaultSpeakerId;
        this.defaultSpeakerId = speakerId;
        try {
            return synthesizeText(text);
        } finally {
            this.defaultSpeakerId = originalSpeakerId;
        }
    }

    private static String cleanTextForTTS(String text) {
        if (text == null || text.trim().isEmpty()) return "";

        String cleanedText = text.codePoints()
                .filter(cp -> !isEmojiOrSpecialSymbol(cp))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        cleanedText = cleanedText
                .replaceAll("\\([^)]*\\)", "")
                .replaceAll("（[^）]*）", "")
                .replaceAll("\\[[^\\]]*\\]", "")
                .replaceAll("【[^】]*】", "")
                .replaceAll("[a-zA-Z]+", "")
                .replaceAll("\\s+", " ")
                .trim();

        boolean hasJapanese = cleanedText.codePoints().anyMatch(TTSService::isJapaneseCharacter);

        if (!hasJapanese) {
            cleanedText = text.codePoints()
                    .filter(cp -> isJapaneseCharacter(cp) || Character.isWhitespace(cp))
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString()
                    .replaceAll("\\s+", " ")
                    .trim();
        }

        return cleanedText;
    }

    private static boolean isEmojiOrSpecialSymbol(int codePoint) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(codePoint);
        if (block == null) return false;

        return block == Character.UnicodeBlock.EMOTICONS
                || block == Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS
                || block == Character.UnicodeBlock.TRANSPORT_AND_MAP_SYMBOLS
                || block == Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS
                || block == Character.UnicodeBlock.DINGBATS
                || block == Character.UnicodeBlock.SUPPLEMENTAL_SYMBOLS_AND_PICTOGRAPHS
                || block == Character.UnicodeBlock.SYMBOLS_AND_PICTOGRAPHS_EXTENDED_A
                || block == Character.UnicodeBlock.GEOMETRIC_SHAPES
                || block == Character.UnicodeBlock.MISCELLANEOUS_TECHNICAL
                || (codePoint >= 0x1F600 && codePoint <= 0x1F64F)
                || (codePoint >= 0x1F300 && codePoint <= 0x1F5FF)
                || (codePoint >= 0x1F680 && codePoint <= 0x1F6FF)
                || (codePoint >= 0x2600 && codePoint <= 0x26FF)
                || (codePoint >= 0x2700 && codePoint <= 0x27BF);
    }

    private static boolean isJapaneseCharacter(int codePoint) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(codePoint);
        if (block == null) return false;

        return block == Character.UnicodeBlock.HIRAGANA
                || block == Character.UnicodeBlock.KATAKANA
                || block == Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || block == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || block == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    public String getAvailableSpeakers() throws IOException {
        String speakersUrl = voicevoxBaseUrl + "/speakers";
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(speakersUrl, String.class);
            if (response.getStatusCode().is2xxSuccessful()) return response.getBody();
            else throw new IOException("화자 목록 조회 실패: HTTP " + response.getStatusCode());
        } catch (Exception e) {
            throw new IOException("화자 목록 조회 실패: " + e.getMessage(), e);
        }
    }

    public boolean isVoicevoxAvailable() {
        String healthUrl = voicevoxBaseUrl + "/speakers";
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    public String previewCleanText(String text) {
        return cleanTextForTTS(text);
    }
}
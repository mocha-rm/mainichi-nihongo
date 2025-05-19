package com.jhlab.mainichi_nihongo.domain.email.scheduler;

import com.jhlab.mainichi_nihongo.domain.content.ContentService;
import com.jhlab.mainichi_nihongo.domain.email.service.EmailService;
import com.jhlab.mainichi_nihongo.domain.subscribe.entity.Subscriber;
import com.jhlab.mainichi_nihongo.domain.subscribe.service.SubscribeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailScheduler {
    private final EmailService emailService;
    private final SubscribeService subscribeService;
    private final ContentService contentService;

    @Scheduled(cron = "0 0 8 * * ?")
    public void sendDailyEmailToSubscribers() {
        log.info("일일 이메일 발송 작업 시작: {}", LocalDateTime.now());

        List<Subscriber> subscribers = subscribeService.getAllSubscribers();

        if (subscribers.isEmpty()) {
            log.info("구독자가 없습니다. 이메일 발송을 건너뜁니다.");
            return;
        }

        String subject = "마이니치 니홍고 - 오늘의 일본어 학습";
        String content = generateEmailContent();

        int successCount = 0;
        int failCount = 0;

        for (Subscriber subscriber : subscribers) {
            boolean sent = emailService.sendEmail(subscriber.getEmail(), subject, content);

            if (sent) {
                successCount++;
            } else {
                failCount++;
            }
        }

        log.info("일일 이메일 발송 완료 - 성공: {}, 실패: {}", successCount, failCount);
    }

    private String generateEmailContent() {
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));

        String dailyContent = contentService.generateDailyContent();

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>마이니치 니홍고 - 오늘의 일본어</title>
                </head>
                <body style="margin: 0; padding: 0; font-family: Arial, sans-serif;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="background-color: #4b0082; padding: 20px; text-align: center; border-radius: 5px 5px 0 0;">
                            <h1 style="color: white; margin: 0;">마이니치 니홍고</h1>
                            <p style="color: #f5f5f5; margin: 5px 0 0 0;">매일 배우는 일본어 - %s</p>
                        </div>
                
                        <div style="background-color: white; padding: 20px; border-radius: 0 0 5px 5px; border: 1px solid #ddd; border-top: none;">
                            %s
                
                            <hr style="border: none; border-top: 1px solid #eee; margin: 30px 0 20px;">
                
                            <p style="font-size: 14px; color: #666; text-align: center;">
                                마이니치 니홍고는 매일 일본어 학습을 돕기 위한 서비스입니다.<br>
                                더 이상 이메일을 받고 싶지 않으시면 <a href="https://mainichi-nihongo.com/unsubscribe" style="color: #4b0082;">여기</a>를 클릭하여 구독을 해지하세요.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(formattedDate, dailyContent);
    }
}

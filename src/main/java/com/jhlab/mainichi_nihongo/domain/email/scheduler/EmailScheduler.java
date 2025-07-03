package com.jhlab.mainichi_nihongo.domain.email.scheduler;

import com.jhlab.mainichi_nihongo.domain.content.service.ContentService;
import com.jhlab.mainichi_nihongo.domain.email.service.EmailService;
import com.jhlab.mainichi_nihongo.domain.subscribe.entity.Subscriber;
import com.jhlab.mainichi_nihongo.domain.subscribe.service.SubscribeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailScheduler {
    private final EmailService emailService;
    private final SubscribeService subscribeService;
    private final ContentService contentService;

    @Scheduled(cron = "0 0 8 * * ?", zone = "Asia/Seoul")
    public void sendDailyEmailToSubscribers() {
        log.info("일일 이메일 발송 작업 시작: {}", LocalDateTime.now());

        List<Subscriber> subscribers = subscribeService.getAllSubscribers();

        if (subscribers.isEmpty()) {
            log.info("구독자가 없습니다. 이메일 발송을 건너뜁니다.");
            return;
        }

        String subject = "마이니치 니홍고 - 오늘의 일본어 학습";
        String content = contentService.generateDailyContent();

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

    public void sendDailyEmailToSubscriber(String email) {
        log.info("일일 이메일 발송 작업 시작: {}", LocalDateTime.now());

        Subscriber subscriber = subscribeService.getSubscriber(email);

        if (subscriber == null) {
            log.info("이메일 발송을 건너뜁니다.");
            return;
        }

        String subject = "마이니치 니홍고 - 오늘의 일본어 학습";
        String content = contentService.generateDailyContent();

        int successCount = 0;
        int failCount = 0;

        boolean sent = emailService.sendEmail(subscriber.getEmail(), subject, content);

        if (sent) {
            successCount++;
        } else {
            failCount++;
        }

        log.info("이메일 발송 완료 - 성공: {}, 실패: {}", successCount, failCount);
    }
}

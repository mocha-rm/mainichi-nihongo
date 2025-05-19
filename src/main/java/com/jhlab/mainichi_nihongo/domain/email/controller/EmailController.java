package com.jhlab.mainichi_nihongo.domain.email.controller;

import com.jhlab.mainichi_nihongo.domain.email.scheduler.EmailScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 이메일 발송을 수동으로 테스트하기 위한 컨트롤러
 * (실제 운영에서는 필요에 따라 보안 설정 추가)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class EmailController {
    private final EmailScheduler emailScheduler;

    /**
     * 이메일 발송을 수동으로 작동
     * (테스트)
     * @return 응답 메시지
     */
    @PostMapping("/send-daily-emails")
    public ResponseEntity<String> sendDailyEmails() {
        emailScheduler.sendDailyEmailToSubscribers();
        return new ResponseEntity<>("이메일 발송을 시작하였습니다.", HttpStatus.OK);
    }
}

package com.jhlab.mainichi_nihongo.domain.subscriber.controller;

import com.jhlab.mainichi_nihongo.domain.subscriber.service.SubscribeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubscribeController {
    private final SubscribeService subscribeService;

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@RequestBody String email) {
        subscribeService.subscribe(email);
        return new ResponseEntity<>("구독 완료! 감사합니다.", HttpStatus.OK);
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribe(@RequestBody String email) {
        subscribeService.unsubscribe(email);
        return new ResponseEntity<>("구독이 해지되었습니다. 감사합니다.", HttpStatus.OK);
    }
}

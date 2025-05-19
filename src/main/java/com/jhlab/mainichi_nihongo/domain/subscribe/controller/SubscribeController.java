package com.jhlab.mainichi_nihongo.domain.subscribe.controller;

import com.jhlab.mainichi_nihongo.domain.subscribe.dto.SubscribeRequestDto;
import com.jhlab.mainichi_nihongo.domain.subscribe.dto.SubscriberResponseDto;
import com.jhlab.mainichi_nihongo.domain.subscribe.dto.UnsubscribeRequestDto;
import com.jhlab.mainichi_nihongo.domain.subscribe.entity.Subscriber;
import com.jhlab.mainichi_nihongo.domain.subscribe.service.SubscribeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubscribeController {
    private final SubscribeService subscribeService;

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@RequestBody SubscribeRequestDto requestDto) {
        subscribeService.subscribe(requestDto.email());
        return new ResponseEntity<>("구독 완료! 감사합니다.", HttpStatus.OK);
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribe(@RequestBody UnsubscribeRequestDto requestDto) {
        subscribeService.unsubscribe(requestDto.email());
        return new ResponseEntity<>("구독이 해지되었습니다. 감사합니다.", HttpStatus.OK);
    }

    @GetMapping("/subscribers")
    public ResponseEntity<List<SubscriberResponseDto>> findSubscribers() {
        List<Subscriber> allSubscribers = subscribeService.getAllSubscribers();

        return new ResponseEntity<>(allSubscribers.stream()
                .map(subscriber -> new SubscriberResponseDto(subscriber.getEmail())).toList(),
                HttpStatus.OK);
    }
}

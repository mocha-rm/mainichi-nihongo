package com.jhlab.mainichi_nihongo.domain.subscribe.service;

import com.jhlab.mainichi_nihongo.domain.subscribe.entity.Subscriber;
import com.jhlab.mainichi_nihongo.domain.subscribe.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscribeService {
    private final SubscriberRepository subscriberRepository;

    @Transactional
    public void subscribe(String email) {
        if (!isValidEmail(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 이메일입니다.");
        }

        if (subscriberRepository.existsByEmail(email)) {
            return;
        }

        subscriberRepository.save(new Subscriber(email));
    }

    @Transactional
    public void unsubscribe(String email) {
        Subscriber subscriber = subscriberRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "등록되지 않은 이메일입니다."));

        subscriberRepository.delete(subscriber);
    }

    @Transactional(readOnly = true)
    public List<Subscriber> getAllSubscribers() {
        return subscriberRepository.findAll();
    }

    public long getSubscribersCount() {
        return subscriberRepository.getCountOfSubscribers();
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]{2,}$");
    }
}

package com.jhlab.mainichi_nihongo.domain.email.repository;

import com.jhlab.mainichi_nihongo.domain.email.entity.EmailContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailContentRepository extends JpaRepository<EmailContent, Long> {
    /**
     * 아직 발송되지 않은 가장 최근에 생성된 이메일 콘텐츠를 가져옵니다.
     */
    Optional<EmailContent> findTopBySentFalseOrderByCreatedAtDesc();

    /**
     * 특정 날짜에 생성된 이메일 콘텐츠를 찾습니다.
     */
    @Query("SELECT e FROM EmailContent e WHERE DATE(e.createdAt) = DATE(?1)")
    Optional<EmailContent> findByCreatedDate(LocalDateTime dateTime);

    /**
     * 마지막으로 발송된 이메일 콘텐츠를 가져옵니다.
     */
    Optional<EmailContent> findTopBySentTrueOrderBySentAtDesc();
}

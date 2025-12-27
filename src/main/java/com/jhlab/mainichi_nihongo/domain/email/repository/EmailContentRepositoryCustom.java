package com.jhlab.mainichi_nihongo.domain.email.repository;

import com.jhlab.mainichi_nihongo.domain.email.entity.EmailContent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmailContentRepositoryCustom {
    Page<EmailContent> findAllWithFilters(String jlptLevel, String topic, Pageable pageable);
}

package com.jhlab.mainichi_nihongo.domain.email.repository;

import com.jhlab.mainichi_nihongo.domain.content.entity.QContentTheme;
import com.jhlab.mainichi_nihongo.domain.email.entity.EmailContent;
import com.jhlab.mainichi_nihongo.domain.email.entity.QEmailContent;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EmailContentRepositoryCustomImpl implements EmailContentRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<EmailContent> findAllWithFilters(String jlptLevel, String topic, Pageable pageable) {
        QEmailContent emailContent = QEmailContent.emailContent;
        QContentTheme theme = QContentTheme.contentTheme;

        // 데이터 조회 쿼리 (fetch join 으로 N+1 문제 해결)
        List<EmailContent> contents = queryFactory
                .selectFrom(emailContent)
                .join(emailContent.theme, theme).fetchJoin()
                .where(
                        jlptLevelEq(jlptLevel, theme),
                        topicEq(topic, theme)
                )
                .orderBy(getOrderSpecifier(emailContent, pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // count 쿼리 (fetch join 없이, 필요할 때만 실행)
        JPAQuery<Long> countQuery = queryFactory
                .select(emailContent.count())
                .from(emailContent)
                .join(emailContent.theme, theme)
                .where(
                        jlptLevelEq(jlptLevel, theme),
                        topicEq(topic, theme)
                );

        // PageableExecutionUtils 를 사용하여 count 쿼리 최적화
        // (마지막 페이지이거나 결과가 pageSize 보다 적으면 count 쿼리 생략)
        return PageableExecutionUtils.getPage(contents, pageable, countQuery::fetchOne);
    }

    /**
     * JLPT 레벨 필터 조건
     * null 이면 조건에서 제외됨
     */
    private BooleanExpression jlptLevelEq(String jlptLevel, QContentTheme theme) {
        return (jlptLevel != null && !jlptLevel.isBlank())
                ? theme.JLPTLevel.eq(jlptLevel)
                : null;
    }

    /**
     * 주제 필터 조건
     * null 이면 조건에서 제외됨
     */
    private BooleanExpression topicEq(String topic, QContentTheme theme) {
        return (topic != null && !topic.isBlank())
                ? theme.topic.eq(topic)
                : null;
    }

    /**
     * 정렬 조건 생성
     * Pageable 의 Sort 정보를 기반으로 OrderSpecifier 생성
     */
    private OrderSpecifier<?> getOrderSpecifier(QEmailContent emailContent, Pageable pageable) {
        if (!pageable.getSort().isEmpty()) {
            return pageable.getSort().stream()
                    .findFirst()
                    .map(order -> {
                        if (order.isAscending()) {
                            return emailContent.createdAt.asc();
                        } else {
                            return emailContent.createdAt.desc();
                        }
                    })
                    .orElse(emailContent.createdAt.desc());
        }
        return emailContent.createdAt.desc();
    }
}

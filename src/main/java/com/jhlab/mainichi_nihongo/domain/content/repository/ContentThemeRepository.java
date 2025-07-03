package com.jhlab.mainichi_nihongo.domain.content.repository;

import com.jhlab.mainichi_nihongo.domain.content.entity.ContentTheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ContentThemeRepository extends JpaRepository<ContentTheme, Long> {
    /**
     * 아직 사용되지 않은 콘텐츠 테마를 랜덤으로 하나 가져옵니다.
     */
    @Query(value = "SELECT * FROM content_theme WHERE used = false ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<ContentTheme> findRandomUnusedTheme();

    /**
     * 특정 JLPT 레벨의 아직 사용되지 않은 콘텐츠 테마를 랜덤으로 하나 가져옵니다.
     */
    @Query(value = "SELECT * FROM content_theme WHERE used = false AND JLPT_LEVEL = ?1 ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<ContentTheme> findRandomUnusedThemeByLevel(String JLPTLevel);

    /**
     * 사용되지 않은 테마가 있는지 체크
     */
    boolean existsByUsedFalse();

    /**
     * 특정 JLPT 레벨의 사용되지 않은 테마가 있는지 체크
     */
    boolean existsByJLPTLevelAndUsedFalse(String JLPTLevel);

    /**
     * 모든 JLPT 레벨 목록을 가져옵니다.
     */
    @Query("SELECT DISTINCT c.JLPTLevel FROM ContentTheme c")
    List<String> findAllJLPTLevels();
}

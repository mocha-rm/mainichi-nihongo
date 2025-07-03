package com.jhlab.mainichi_nihongo.domain.email.controller;

import com.jhlab.mainichi_nihongo.domain.email.test.EmailPerformanceTest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/email/performance")
@RequiredArgsConstructor
public class EmailPerformanceTestController {
    private final EmailPerformanceTest emailPerformanceTest;

    /**
     * 종합 성능 테스트 실행
     */
    @PostMapping("/comprehensive")
    public ResponseEntity<Map<String, Object>> runComprehensiveTest() {
        try {
            log.info("종합 성능 테스트 시작 요청 받음");
            emailPerformanceTest.runComprehensivePerformanceTest();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "종합 성능 테스트가 완료되었습니다. 로그를 확인해주세요.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("종합 성능 테스트 중 오류 발생", e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "성능 테스트 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 단일 스레드 성능 테스트
     */
    @PostMapping("/single-thread")
    public ResponseEntity<Map<String, Object>> runSingleThreadTest(@RequestParam(defaultValue = "1000") int count) {
        try {
            log.info("단일 스레드 성능 테스트 시작: {}명", count);
            EmailPerformanceTest.PerformanceResult result = emailPerformanceTest.testSingleThreadPerformance(count);

            Map<String, Object> response = createResultResponse(result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    /**
     * 멀티 스레드 성능 테스트
     */
    @PostMapping("/multi-thread")
    public ResponseEntity<Map<String, Object>> runMultiThreadTest(
            @RequestParam(defaultValue = "10000") int count,
            @RequestParam(defaultValue = "10") int threadCount) {
        try {
            log.info("멀티 스레드 성능 테스트 시작: {}명, {}개 스레드", count, threadCount);
            EmailPerformanceTest.PerformanceResult result = emailPerformanceTest.testMultiThreadPerformance(count, threadCount);

            Map<String, Object> response = createResultResponse(result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    /**
     * 배치 처리 성능 테스트
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> runBatchTest(
            @RequestParam(defaultValue = "10000") int count,
            @RequestParam(defaultValue = "100") int batchSize,
            @RequestParam(defaultValue = "10") int threadCount) {
        try {
            log.info("배치 처리 성능 테스트 시작: {}명, 배치크기: {}, {}개 스레드", count, batchSize, threadCount);
            EmailPerformanceTest.PerformanceResult result = emailPerformanceTest.testBatchPerformance(count, batchSize, threadCount);

            Map<String, Object> response = createResultResponse(result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    /**
     * 시스템 리소스 정보 조회
     */
    @GetMapping("/system-info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Runtime runtime = Runtime.getRuntime();

        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("availableProcessors", runtime.availableProcessors());
        systemInfo.put("maxMemory", formatBytes(runtime.maxMemory()));
        systemInfo.put("totalMemory", formatBytes(runtime.totalMemory()));
        systemInfo.put("freeMemory", formatBytes(runtime.freeMemory()));
        systemInfo.put("usedMemory", formatBytes(runtime.totalMemory() - runtime.freeMemory()));

        // JVM 정보
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("jvmName", System.getProperty("java.vm.name"));
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osArch", System.getProperty("os.arch"));

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("systemInfo", systemInfo);

        return ResponseEntity.ok(response);
    }

    /**
     * 성능 테스트 결과를 응답 형태로 변환
     */
    private Map<String, Object> createResultResponse(EmailPerformanceTest.PerformanceResult result) {
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("testType", result.getTestType());
        resultData.put("targetCount", result.getTargetCount());
        resultData.put("successCount", result.getSuccessCount());
        resultData.put("failCount", result.getFailCount());
        resultData.put("executionTimeMs", result.getExecutionTime());
        resultData.put("executionTimeSeconds", result.getExecutionTime() / 1000.0);
        resultData.put("throughputPerSecond", String.format("%.2f", result.getThroughput()));
        resultData.put("memoryUsageMB", String.format("%.2f", result.getMemoryUsage() / (1024.0 * 1024.0)));
        resultData.put("successRate", String.format("%.2f%%", result.getSuccessRate()));
        resultData.put("threadCount", result.getThreadCount());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("result", resultData);

        return response;
    }

    /**
     * 오류 응답 생성
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(Exception e) {
        log.error("성능 테스트 중 오류 발생", e);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "성능 테스트 중 오류가 발생했습니다: " + e.getMessage());

        return ResponseEntity.internalServerError().body(response);
    }

    /**
     * 바이트를 읽기 쉬운 형태로 변환
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}

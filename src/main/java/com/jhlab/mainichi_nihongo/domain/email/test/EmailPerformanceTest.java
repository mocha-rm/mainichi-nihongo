package com.jhlab.mainichi_nihongo.domain.email.test;

import com.jhlab.mainichi_nihongo.domain.content.service.ContentService;
import com.jhlab.mainichi_nihongo.domain.email.service.EmailService;
import com.jhlab.mainichi_nihongo.domain.subscribe.entity.Subscriber;
import com.jhlab.mainichi_nihongo.domain.subscribe.service.SubscribeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailPerformanceTest {
    private final EmailService emailService;
    private final SubscribeService subscribeService;
    private final ContentService contentService;

    /**
     * 단일 스레드 성능 테스트 (기존)
     */
    public PerformanceResult testSingleThreadPerformance(int targetCount) {
        log.info("=== 단일 스레드 성능 테스트 시작 (목표: {}명) ===", targetCount);

        List<Subscriber> testSubscribers = createTestSubscribers(targetCount);
        String subject = "마이니치 니홍고 - 성능 테스트";
        String content = contentService.generateDailyContent();

        long startTime = System.currentTimeMillis();
        long startMemory = getUsedMemory();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (Subscriber subscriber : testSubscribers) {
            boolean sent = emailService.sendEmail(subscriber.getEmail(), subject, content);
            if (sent) {
                successCount.incrementAndGet();
            } else {
                failCount.incrementAndGet();
            }

            if ((successCount.get() + failCount.get()) % 1000 == 0) {
                log.info("진행률: {}/{}", successCount.get() + failCount.get(), targetCount);
            }
        }

        long endTime = System.currentTimeMillis();
        long endMemory = getUsedMemory();

        return new PerformanceResult(
                "Single Thread",
                targetCount,
                successCount.get(),
                failCount.get(),
                endTime - startTime,
                endMemory - startMemory,
                1
        );
    }

    /**
     * 멀티 스레드 성능 테스트 (병렬 처리)
     */
    public PerformanceResult testMultiThreadPerformance(int targetCount, int threadCount) {
        log.info("=== 멀티 스레드 성능 테스트 시작 (목표: {}명, 스레드: {}개) ===", targetCount, threadCount);

        List<Subscriber> testSubscribers = createTestSubscribers(targetCount);
        String subject = "마이니치 니홍고 - 성능 테스트 (멀티스레드)";
        String content = contentService.generateDailyContent();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        long startTime = System.currentTimeMillis();
        long startMemory = getUsedMemory();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger processedCount = new AtomicInteger(0);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Subscriber subscriber : testSubscribers) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    boolean sent = emailService.sendEmail(subscriber.getEmail(), subject, content);
                    if (sent) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }

                    int processed = processedCount.incrementAndGet();
                    if (processed % 1000 == 0) {
                        log.info("진행률: {}/{}", processed, targetCount);
                    }
                } catch (Exception e) {
                    log.error("이메일 발송 중 오류 발생: {}", e.getMessage());
                    failCount.incrementAndGet();
                    processedCount.incrementAndGet();
                }
            }, executor);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long endTime = System.currentTimeMillis();
        long endMemory = getUsedMemory();

        executor.shutdown();

        return new PerformanceResult(
                "Multi Thread",
                targetCount,
                successCount.get(),
                failCount.get(),
                endTime - startTime,
                endMemory - startMemory,
                threadCount
        );
    }

    /**
     * 배치 처리 성능 테스트
     */
    public PerformanceResult testBatchPerformance(int targetCount, int batchSize, int threadCount) {
        log.info("=== 배치 처리 성능 테스트 시작 (목표: {}명, 배치크기: {}, 스레드: {}개) ===",
                targetCount, batchSize, threadCount);

        List<Subscriber> testSubscribers = createTestSubscribers(targetCount);
        String subject = "마이니치 니홍고 - 성능 테스트 (배치)";
        String content = contentService.generateDailyContent();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        long startTime = System.currentTimeMillis();
        long startMemory = getUsedMemory();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // 배치 단위로 나누어 처리
        for (int i = 0; i < testSubscribers.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, testSubscribers.size());
            List<Subscriber> batch = testSubscribers.subList(i, endIndex);

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                int batchSuccess = 0;
                int batchFail = 0;

                for (Subscriber subscriber : batch) {
                    try {
                        boolean sent = emailService.sendEmail(subscriber.getEmail(), subject, content);
                        if (sent) {
                            batchSuccess++;
                        } else {
                            batchFail++;
                        }
                    } catch (Exception e) {
                        log.error("배치 처리 중 오류 발생: {}", e.getMessage());
                        batchFail++;
                    }
                }

                successCount.addAndGet(batchSuccess);
                failCount.addAndGet(batchFail);

                log.info("배치 완료 - 성공: {}, 실패: {}, 전체 진행률: {}/{}",
                        batchSuccess, batchFail, successCount.get() + failCount.get(), targetCount);

            }, executor);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long endTime = System.currentTimeMillis();
        long endMemory = getUsedMemory();

        executor.shutdown();

        return new PerformanceResult(
                "Batch Processing",
                targetCount,
                successCount.get(),
                failCount.get(),
                endTime - startTime,
                endMemory - startMemory,
                threadCount
        );
    }

    /**
     * 종합 성능 테스트 실행
     */
    public void runComprehensivePerformanceTest() {
        log.info("=== 종합 성능 테스트 시작 ===");

        List<PerformanceResult> results = new ArrayList<>();

        // 1. 단일 스레드 테스트 (1000명)
        results.add(testSingleThreadPerformance(1000));

        // 2. 멀티 스레드 테스트 (10000명, 다양한 스레드 수)
        results.add(testMultiThreadPerformance(10000, 5));
        results.add(testMultiThreadPerformance(10000, 10));
        results.add(testMultiThreadPerformance(10000, 20));

        // 3. 배치 처리 테스트 (10000명)
        results.add(testBatchPerformance(10000, 100, 10));
        results.add(testBatchPerformance(10000, 500, 10));

        // 결과 출력
        printPerformanceReport(results);
    }

    /**
     * 테스트용 구독자 목록 생성
     */
    private List<Subscriber> createTestSubscribers(int count) {
        List<Subscriber> testSubscribers = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            Subscriber subscriber = new Subscriber(String.format("tester%d@testmail.com", i));
            testSubscribers.add(subscriber);
        }

        return testSubscribers;
    }

    /**
     * 현재 메모리 사용량 조회
     */
    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    /**
     * 성능 테스트 결과 출력
     */
    private void printPerformanceReport(List<PerformanceResult> results) {
        log.info("\n" + "=".repeat(80));
        log.info("                    성능 테스트 결과 리포트");
        log.info("=".repeat(80));
        log.info("테스트 일시: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        log.info("-".repeat(80));

        for (PerformanceResult result : results) {
            log.info("테스트 방식: {}", result.getTestType());
            log.info("대상 인원: {}명", result.getTargetCount());
            log.info("스레드 수: {}개", result.getThreadCount());
            log.info("성공: {}명, 실패: {}명", result.getSuccessCount(), result.getFailCount());
            log.info("소요 시간: {}ms ({:.2f}초)", result.getExecutionTime(), result.getExecutionTime() / 1000.0);
            log.info("처리량: {:.2f}명/초", result.getThroughput());
            log.info("메모리 사용량: {:.2f}MB", result.getMemoryUsage() / (1024.0 * 1024.0));
            log.info("성공률: {:.2f}%", result.getSuccessRate());
            log.info("-".repeat(80));
        }

        log.info("=".repeat(80));
    }

    /**
     * 성능 테스트 결과 데이터 클래스
     */
    public static class PerformanceResult {
        private final String testType;
        private final int targetCount;
        private final int successCount;
        private final int failCount;
        private final long executionTime;
        private final long memoryUsage;
        private final int threadCount;

        public PerformanceResult(String testType, int targetCount, int successCount, int failCount,
                                 long executionTime, long memoryUsage, int threadCount) {
            this.testType = testType;
            this.targetCount = targetCount;
            this.successCount = successCount;
            this.failCount = failCount;
            this.executionTime = executionTime;
            this.memoryUsage = memoryUsage;
            this.threadCount = threadCount;
        }

        public double getThroughput() {
            return executionTime > 0 ? (double) targetCount / (executionTime / 1000.0) : 0;
        }

        public double getSuccessRate() {
            return targetCount > 0 ? (double) successCount / targetCount * 100 : 0;
        }

        // Getters
        public String getTestType() { return testType; }
        public int getTargetCount() { return targetCount; }
        public int getSuccessCount() { return successCount; }
        public int getFailCount() { return failCount; }
        public long getExecutionTime() { return executionTime; }
        public long getMemoryUsage() { return memoryUsage; }
        public int getThreadCount() { return threadCount; }
    }
}

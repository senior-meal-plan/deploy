package io.github.tlsdla1235.seniormealplan.service.orchestration;


import io.github.tlsdla1235.seniormealplan.domain.Meal;
import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.report.DailyReport;
import io.github.tlsdla1235.seniormealplan.dto.async.DailyReportGenerationData;
import io.github.tlsdla1235.seniormealplan.dto.dailyreport.DailyReportAnalysisResultDto;
import io.github.tlsdla1235.seniormealplan.repository.MealRepository;
import io.github.tlsdla1235.seniormealplan.service.food.MealService;
import io.github.tlsdla1235.seniormealplan.service.report.DailyReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenerateDailyReportService {

    private final DailyReportService dailyReportService;
    private final MealService mealService;
    private final ReportAsyncService reportAsyncService;
    private static final int CHUNK_SIZE = 5;
    private final MealRepository mealRepository;


    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    public void generateDailyReportsBatch() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("데일리 리포트 배치 작업 시작. 대상 날짜: {}", yesterday);

        // 1. 어제 식사한 유저 조회
        List<User> usersToReport = mealService.findUsersWithMealsOnDate(yesterday);

        if (usersToReport.isEmpty()) {
            log.info("배치 작업: {} 날짜에 식사 기록이 있는 유저가 없습니다.", yesterday);
            return;
        }
        log.info("배치 작업: 총 {}명의 유저에 대한 리포트를 생성합니다.", usersToReport.size());

        // 2. [변경] 트랜잭션 메서드를 호출하여 모든 리포트를 '먼저' 생성
        List<DailyReportGenerationData> generatedData = dailyReportService.createPendingReportsInTransaction(usersToReport, yesterday);

        if (generatedData.isEmpty()) {
            log.info("배치 작업: 생성된 리포트가 없어 API 호출을 생략합니다.");
            return;
        }


        log.info("생성된 리포트 데이터 DTO 리스트 상세 (총 {}개):", generatedData.size());
        for (DailyReportGenerationData data : generatedData) {
                log.info("  - DTO: {}", data);
            }
        reportAsyncService.requestBatchAnalysis(generatedData);
    }



    @Transactional
    public void updateDailyReportWithAnalysis(DailyReportAnalysisResultDto result) {
        dailyReportService.updateReportWithAnalysis(result);
    }


    /**
     * 테스트용. 추후 삭제
     * @param user
     * @param date
     */
    @Transactional
    public void generateReportAndRequestAnalysis(User user, LocalDate date) {
        List<Meal> meals = mealService.findByUserAndMealDateWithFoods(user, date);
        // 식사 기록이 없으면 리포트를 생성하지 않습니다.
        if (meals.isEmpty()) {
            log.info("유저 ID: {}에 대한 식사기록이 없스빈다. {}에 대한 데일리 리포트 생성을 하지 않습니다", user.getUserId(), date);
            return;
        }
        DailyReport report = dailyReportService.createPendingDailyReport(user, date);
        user.setLastDailyReportDate(date);
        log.info("Updated last daily report date for user ID: {} to {}", user.getUserId(), date);
    }

    @Scheduled(cron = "0 41 23 * * *", zone = "Asia/Seoul")
    public void scheduleTestFor1130PM() {
        LocalDateTime time = LocalDateTime.now();
        log.info("========== 스케줄 테스트 ==========");
        log.info("현재 시간 오후 11시 30분, 디버깅 메시지 출력 성공!: {}", time);
        log.info("===================================");
    }

    @Transactional
    public void generateDailyReportsBatchTest(LocalDate date) {

        log.info("데일리 리포트 배치 작업 시작. 대상 날짜: {}", date);
        // 1. 어제 식사한 유저 조회
        List<User> usersToReport = mealService.findUsersWithMealsOnDate(date);
        if (usersToReport.isEmpty()) {
            log.info("배치 작업: {} 날짜에 식사 기록이 있는 유저가 없습니다.", date);
            return;
        }
        log.info("배치 작업: 총 {}명의 유저에 대한 리포트를 생성합니다.", usersToReport.size());
        // 2. [변경] 트랜잭션 메서드를 호출하여 모든 리포트를 '먼저' 생성
        List<DailyReportGenerationData> generatedData = dailyReportService.createPendingReportsInTransaction(usersToReport, date);

        if (generatedData.isEmpty()) {
            log.info("배치 작업: 생성된 리포트가 없어 API 호출을 생략합니다.");
            return;
        }


        log.info("생성된 리포트 데이터 DTO 리스트 상세 (총 {}개):", generatedData.size());
        for (DailyReportGenerationData data : generatedData) {
            log.info("  - DTO: {}", data);
        }
        reportAsyncService.requestBatchAnalysis(generatedData);
    }



    @Transactional
    public void executeBatch_Before(LocalDate date) {
        long startTime = System.currentTimeMillis();
        log.info("[Batch-Before] 비효율적 배치 시작. (단일 트랜잭션, 전체 로딩)");

        List<User> usersToReport = mealService.findUsersWithMealsOnDate(date);

        if (usersToReport.isEmpty()) {
            log.info("[Batch-Before] 대상 유저가 없습니다.");
            return;
        }
        log.info("[Batch-Before] 총 {}명의 유저를 메모리에 적재했습니다.", usersToReport.size());
        List<DailyReportGenerationData> generatedData = dailyReportService.createPendingReportsInTransaction(usersToReport, date);

        if (!generatedData.isEmpty()) {
            reportAsyncService.requestBatchAnalysis(generatedData);
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("[Batch-Before] 배치 종료. 소요 시간: {}ms", duration);
    }

    public void executeBatch_After(LocalDate date) {
        long startTime = System.currentTimeMillis();
        log.info("[Batch-After] 최적화 배치 시작. (Chunk Size: {}, 트랜잭션 분리)", CHUNK_SIZE);

        int pageNumber = 0;
        Slice<User> userPage;
        int totalProcessed = 0;

        do {
            userPage = mealRepository.findDistinctUserByMealDate(date, PageRequest.of(pageNumber, CHUNK_SIZE));
            List<User> usersChunk = userPage.getContent();

            if (usersChunk.isEmpty()) break;

            List<DailyReportGenerationData> chunkData = dailyReportService.createReportsForChunk(usersChunk, date);

            if (!chunkData.isEmpty()) {
                reportAsyncService.requestBatchAnalysis(chunkData);
            }

            totalProcessed += usersChunk.size();
            log.info("[Batch-After] Chunk {} 처리 완료 (누적 {}명).", pageNumber + 1, totalProcessed);

            pageNumber++;

            chunkData = null;
            usersChunk = null;

        } while (userPage.hasNext());

        long duration = System.currentTimeMillis() - startTime;
        log.info("[Batch-After] 배치 종료. 총 {}명 처리 완료. 소요 시간: {}ms", totalProcessed, duration);
    }
}

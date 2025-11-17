package io.github.tlsdla1235.seniormealplan.service.orchestration;

import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.report.WeeklyReport;
import io.github.tlsdla1235.seniormealplan.dto.async.WeeklyReportGenerationData;
import io.github.tlsdla1235.seniormealplan.dto.user.WhoAmIDto;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.AnalysisResultDto.WeeklyAnalysisResultDto;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.DailyReportsForWeeklyReportDto;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.MealForWeeklyDto;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.WeeklyReportGenerateRequestDto;
import io.github.tlsdla1235.seniormealplan.service.food.FoodService;
import io.github.tlsdla1235.seniormealplan.service.food.MealService;
import io.github.tlsdla1235.seniormealplan.service.recipe.RecipeRecommendService;
import io.github.tlsdla1235.seniormealplan.service.report.DailyReportService;
import io.github.tlsdla1235.seniormealplan.service.report.MealReportService;
import io.github.tlsdla1235.seniormealplan.service.report.WeeklyReportService;
import io.github.tlsdla1235.seniormealplan.service.user.AiManagementGoalService;
import io.github.tlsdla1235.seniormealplan.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateWeeklyReportsService {
    private final WeeklyReportService weeklyReportService;
    private final DailyReportService dailyReportService;
    private final MealService mealService;
    private final UserService userService;
    private final AiManagementGoalService aiManagementGoalService;
    private final RecipeRecommendService recipeRecommendService;
    private final ReportAsyncService reportAsyncService;

    @Value("${service.weekly.analysis.url}")
    private String analysisApiUrl; // FastAPI 서버 주소 (application.yml)

    @Value("${service.webhook.callback-url}")
    private String webhookCallbackUrl; // 우리 웹훅 주소 (application.yml)


    /**
     * 테스트용 함수
     * @param user
     */
    @Transactional
    public void createRequestDto(User user)
    {
        LocalDate today = LocalDate.now();
        log.info("Create Weekly Report request dto");
        WhoAmIDto userdto = userService.whoAmI(user);
        log.info("userdto: {}", userdto);
        List<DailyReportsForWeeklyReportDto> dailyReportDto = dailyReportService.getCompletedReportsForCurrentWeek(user, today);
        log.info("dailyReportDto: {}", dailyReportDto);
        List<MealForWeeklyDto> mealsDto = mealService.getMealsForCurrentWeek(user, today);
        log.info("mealsDto: {}", mealsDto);

        WeeklyReport newReport = weeklyReportService.createPendingWeeklyReport(user,today);

        WeeklyReportGenerateRequestDto requestDto = new WeeklyReportGenerateRequestDto(newReport.getReportId(),userdto, dailyReportDto, mealsDto);
    }




    @Scheduled(cron = "0 0 4 * * MON", zone = "Asia/Seoul")
    public void generateWeeklyReportsBatch() {
        ZoneId seoulZone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(seoulZone); // (오늘 = 월요일)

        // 1. '지난주'의 시작(월)과 끝(일) 날짜 계산
        LocalDate lastWeekMonday = today.minusDays(7);
        LocalDate lastWeekSunday = today.minusDays(1);

        log.info("주간 리포트 배치 작업 시작. 대상 기간: {} ~ {}", lastWeekMonday, lastWeekSunday);

        // 2. [필수] 지난주에 식사 기록이 있는 유저 조회
        // (MealService/Repository에 findUsersWithMealsBetweenDates 추가 필요)
        List<User> usersToReport =
                mealService.findUsersWithMealsBetweenDates(lastWeekMonday, lastWeekSunday);

        if (usersToReport.isEmpty()) {
            log.info("배치 작업: 지난주({})에 식사 기록이 있는 유저가 없습니다.", lastWeekMonday);
            return;
        }
        log.info("배치 작업: 총 {}명의 유저에 대한 주간 리포트를 생성합니다.", usersToReport.size());

        // 3. 트랜잭션 메서드를 호출하여 모든 리포트 '먼저' 생성
        // (today를 기준으로 전달하면 내부에서 '지난주'를 계산함)
        List<WeeklyReportGenerationData> generatedData =weeklyReportService.createPendingWeeklyReportsInTransaction(usersToReport, today);

        if (generatedData.isEmpty()) {
            log.info("배치 작업: 생성된 리포트가 없어 API 호출을 생략합니다.");
            return;
        }

        reportAsyncService.requestWeeklyBatchAnalysis(generatedData);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateWeeklyResult(WeeklyAnalysisResultDto resultDto)
    {
        User user = User.builder().userId(resultDto.UserId()).build();
        weeklyReportService.updatePendingWeeklyReport(resultDto);
        aiManagementGoalService.updateAiSelectedTopics(user, resultDto.aiRecommendTopic());
        recipeRecommendService.createWeeklyRecommendations(user, resultDto);
    }

    @Transactional
    public WeeklyReportGenerationData generateWeeklyReportsForTest(User user, LocalDate startDate){
        log.info("테스트용 주간 리포트 배치 작업 시작. 대상 기간: {}의 이전주 월요일 부터 일요일", startDate);
        return weeklyReportService.createWeeklyReportsForTest(user, startDate);
    }

    @Transactional
    public List<WeeklyReportGenerationData> generateWeeklyReportsBatchTest() {
        ZoneId seoulZone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(seoulZone); // (오늘 = 월요일)

        // 1. '지난주'의 시작(월)과 끝(일) 날짜 계산
        LocalDate lastWeekMonday = today.minusDays(7);
        LocalDate lastWeekSunday = today.minusDays(1);

        LocalDate thisWeekMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate thisWeekSunday = thisWeekMonday.plusDays(6);

        log.info("주간 리포트 배치 작업 시작. 대상 기간: {} ~ {}", thisWeekMonday, thisWeekSunday);

        // 2. [필수] 지난주에 식사 기록이 있는 유저 조회
        // (MealService/Repository에 findUsersWithMealsBetweenDates 추가 필요)
        List<User> usersToReport =
                mealService.findUsersWithMealsBetweenDates(thisWeekMonday, thisWeekSunday);

        if (usersToReport.isEmpty()) {
            log.info("배치 작업: 지난주({})에 식사 기록이 있는 유저가 없습니다.", thisWeekSunday);
            return null;
        }
        log.info("배치 작업: 총 {}명의 유저에 대한 주간 리포트를 생성합니다.", usersToReport.size());

        // 3. 트랜잭션 메서드를 호출하여 모든 리포트 '먼저' 생성
        List<WeeklyReportGenerationData> generatedData =weeklyReportService.createPendingWeeklyReportsInTransaction(usersToReport, thisWeekSunday.plusDays(7));

        if (generatedData.isEmpty()) {
            log.info("배치 작업: 생성된 리포트가 없어 API 호출을 생략합니다.");
            return null;
        }
        System.out.println("-----------------");
        System.out.println(thisWeekSunday);
        reportAsyncService.requestWeeklyBatchAnalysis(generatedData);
        return generatedData;
    }
}



//    @Async
//    public void requestAnalysisToExternalApi(WeeklyReport report, WeeklyReportGenerateRequestDto requestDto)
//    {
//        log.info("Starting async request for weekly report analysis. Report ID: {}", report.getReportId());
//
//        webClient.post()
//                .uri(analysisApiUrl)
//                .bodyValue(requestDto)
//                .retrieve()
//                .bodyToMono(Void.class)
//                .doOnError(error -> log.error("Failed to request daily report analysis for report ID: {}", report.getReportId(), error))
//                .subscribe(
//                        result -> log.info("Successfully requested analysis for report ID: {}", report.getReportId()),
//                        error -> {}
//                );
//    }
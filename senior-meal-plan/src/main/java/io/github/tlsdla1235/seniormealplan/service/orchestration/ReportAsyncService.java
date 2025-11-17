package io.github.tlsdla1235.seniormealplan.service.orchestration;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tlsdla1235.seniormealplan.domain.Meal;
import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.report.DailyReport;
import io.github.tlsdla1235.seniormealplan.dto.async.DailyReportGenerationData;
import io.github.tlsdla1235.seniormealplan.dto.async.MealSavedEvent;
import io.github.tlsdla1235.seniormealplan.dto.async.WeeklyReportGenerationData;
import io.github.tlsdla1235.seniormealplan.dto.dailyreport.DailyReportAnalysisRequestDto;
import io.github.tlsdla1235.seniormealplan.dto.meal.AnalysisMealRequestDto;
import io.github.tlsdla1235.seniormealplan.dto.meal.AnalyzedFoodDto;
import io.github.tlsdla1235.seniormealplan.dto.meal.MealDto;
import io.github.tlsdla1235.seniormealplan.dto.user.WhoAmIDto;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.WeeklyReportGenerateRequestDto;
import io.github.tlsdla1235.seniormealplan.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportAsyncService {
    private final WebClient webClient;
    private final UserService userService;

    private final ObjectMapper objectMapper;

    @Value("${service.webhook.callback-url}")
    private String webhookCallbackUrl;

    @Value("${service.meal.analysis.url}")
    private String mealAnalysisApiUrl;

    @Value("${service.daily.analysis.url}")
    private String dailyAnalysisApiUrl; // FastAPI 서버 주소 (application.yml)

    @Value("${service.weekly.analysis.url}")
    private String weeklyAnalysisApiUrl; // FastAPI 서버 주소 (application.yml)

    //이하 밀 리포트

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMealSavedEvent(MealSavedEvent event) {
        Meal meal = event.getMeal();
        User user = event.getUser();
        log.info("MealSavedEvent 수신. 비동기 분석 요청 시작. mealId: {}", meal.getMealId());

        WhoAmIDto whoAmIDto = userService.whoAmI(user);
        AnalysisMealRequestDto requestDto = new AnalysisMealRequestDto(
                meal.getMealId(),
                meal.getPhotoUrl(),
                webhookCallbackUrl,
                whoAmIDto
        );

        webClient.post()
                .uri(mealAnalysisApiUrl)
                .bodyValue(requestDto)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(error -> log.error("Failed to request meal analysis for mealId: {}", meal.getMealId(), error))
                .subscribe();
    }

    // 이하 데일리 리포트

    @Async
    public void requestBatchAnalysis(List<DailyReportGenerationData> reportDataList) {
        if (reportDataList == null || reportDataList.isEmpty()) {
            log.info("분석 요청할 데일리 리포트 배치가 없습니다.");
            return;
        }

        log.info("총 {}건의 데일리 리포트 배치 분석을 비동기로 요청합니다.", reportDataList.size());

        // 1. DTO 리스트 변환
        List<DailyReportAnalysisRequestDto> dtoList = reportDataList.stream()
                .map(this::convertDataToRequestDto)
                .collect(Collectors.toList());

        try {
            String jsonBody = objectMapper.writeValueAsString(dtoList);
            log.info("FastAPI로 전송될 실제 Request Body (JSON): \n{}", jsonBody);
        } catch (JsonProcessingException e) {
            log.warn("Request DTO를 JSON으로 변환하는 데 실패했습니다.", e);
        }

        // 2. [핵심] API에 'DTO 리스트'를 단일 요청으로 전송
        webClient.post()
                .uri(dailyAnalysisApiUrl) // FastAPI 엔드포인트
                .bodyValue(dtoList) // [중요] 단일 DTO가 아닌 List<DTO>를 body에 담아 전송
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(error -> log.error("데일리 리포트 배치 분석 요청 실패", error))
                .subscribe(
                        result -> log.info("총 {}건의 데일리 리포트 배치 분석 요청 성공", dtoList.size()),
                        error -> {}
                );
    }

    private DailyReportAnalysisRequestDto convertDataToRequestDto(DailyReportGenerationData data) {
        User user = data.getUser();
        List<Meal> meals = data.getMeals();
        DailyReport report = data.getReport();

        WhoAmIDto whoAmIDto = userService.whoAmI(user);

        List<MealDto> mealDtos = meals.stream()
                .map(meal -> new MealDto(
                        meal.getMealType(),
                        meal.getMealTime(),
                        meal.getPhotoUrl(),
                        meal.getFoods().stream()
                                .map(food -> new AnalyzedFoodDto(
                                        food.getName(),
                                        food.getKcal(),
                                        food.getProtein(),
                                        food.getCarbs(),
                                        food.getFat(),
                                        food.getCalcium(),
                                        food.getServingSize(),
                                        food.getSaturatedFatPercentKcal(),
                                        food.getUnsaturatedFat(),
                                        food.getDietaryFiber(),
                                        food.getSodium(),
                                        food.getAddedSugarKcal(),
                                        food.getProcessedMeatGram(),
                                        food.getVitaminD_IU(),
                                        food.isVegetable(),
                                        food.isFruit(),
                                        food.isFried()
                                ))
                                .toList()
                ))
                .toList();

        return new DailyReportAnalysisRequestDto(
                report.getReportId(),
                whoAmIDto,
                mealDtos,
                webhookCallbackUrl
        );
    }

    
    //이하 위클리 리포트

    @Async
    public void requestWeeklyBatchAnalysis(List<WeeklyReportGenerationData> reportDataList) {
        if (reportDataList == null || reportDataList.isEmpty()) {
            log.info("분석 요청할 주간 리포트 배치가 없습니다.");
            return;
        }
        log.info("----------------------");
        log.info("{}",reportDataList.get(0).toString());
        log.info("----------------------");
        log.info("총 {}건의 주간 리포트 배치 분석을 비동기로 요청합니다.", reportDataList.size());

        // 1. DTO 리스트 변환
        List<WeeklyReportGenerateRequestDto> dtoList = reportDataList.stream()
                .map(this::convertDataToWeeklyRequestDto) // 헬퍼 메서드 사용
                .collect(Collectors.toList());

        try {
            String jsonBody = objectMapper.writeValueAsString(dtoList);
            log.info("FastAPI(주간)로 전송될 실제 Request Body (JSON): \n{}", jsonBody);
        } catch (JsonProcessingException e) {
            log.warn("Request DTO(주간)를 JSON으로 변환하는 데 실패했습니다.", e);
        }

        // 2. [핵심] API에 'DTO 리스트'를 단일 요청으로 전송
        webClient.post()
                .uri(weeklyAnalysisApiUrl) // [수정] 주간 리포트 URL 사용
                .bodyValue(dtoList) // 리스트 전송
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(error -> log.error("주간 리포트 배치 분석 요청 실패", error))
                .subscribe(
                        result -> log.info("총 {}건의 주간 리포트 배치 분석 요청 성공", dtoList.size()),
                        error -> {}
                );
    }

    private WeeklyReportGenerateRequestDto convertDataToWeeklyRequestDto(WeeklyReportGenerationData data) {
        return new WeeklyReportGenerateRequestDto(
                data.getReport().getReportId(), // [수정] ID 추가
                data.getUserDto(),
                data.getDailyReportDto(),
                data.getMealsDto()
        );
    }
}

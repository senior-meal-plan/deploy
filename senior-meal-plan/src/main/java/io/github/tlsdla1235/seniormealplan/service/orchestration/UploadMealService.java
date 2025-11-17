package io.github.tlsdla1235.seniormealplan.service.orchestration;

import com.amazonaws.services.s3.AmazonS3Client;
import io.github.tlsdla1235.seniormealplan.domain.Meal;
import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.report.MealReport;
import io.github.tlsdla1235.seniormealplan.dto.async.MealSavedEvent;
import io.github.tlsdla1235.seniormealplan.dto.meal.AnalysisMealRequestDto;
import io.github.tlsdla1235.seniormealplan.dto.meal.AnalysisMealResultDto;
import io.github.tlsdla1235.seniormealplan.dto.meal.MealCreateRequest;
import io.github.tlsdla1235.seniormealplan.dto.s3dto.PresignedUrlResponse;
import io.github.tlsdla1235.seniormealplan.dto.user.WhoAmIDto;
import io.github.tlsdla1235.seniormealplan.repository.MealRepository;
import io.github.tlsdla1235.seniormealplan.service.admin.S3UploadService;
import io.github.tlsdla1235.seniormealplan.service.food.FoodService;
import io.github.tlsdla1235.seniormealplan.service.food.MealService;
import io.github.tlsdla1235.seniormealplan.service.report.MealReportService;
import io.github.tlsdla1235.seniormealplan.service.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadMealService {
    private final S3UploadService s3UploadService;
    private final MealRepository mealRepository;
    private final MealReportService mealReportService;
    private final WebClient webClient;
    private final FoodService foodService;
    private final MealService mealService;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${service.meal.analysis.url}")
    private String analysisApiUrl; // FastAPI 서버 주소 (application.yml)

    @Value("${service.webhook.callback-url}")
    private String webhookCallbackUrl; // 우리 웹훅 주소 (application.yml)


    public PresignedUrlResponse generatePresignedUrl(String originalFileName){
        return s3UploadService.generatePresignedUrl(originalFileName);
    }

    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "todayMeals", key = "#meal.user.userId",
                            condition = "#meal.mealDate.isEqual(T(java.time.LocalDate).now())"),
                    @CacheEvict(value = "mealsByDate", key = "#meal.user.userId + '_' + #meal.mealDate.toString()")
            }
    )
    public Meal saveMeal(Meal meal, String uniqueFileName){
        String url = s3UploadService.getFileUrl(uniqueFileName);
        meal.setPhotoUrl(url);
        meal.setUniqueFileName(uniqueFileName);
        mealRepository.save(meal);
        log.info("사용자 id {}에 대한 meal이 생성되었습니다. mealid ={}", meal.getUser().getUserId(), meal.getMealId());
        MealReport mealReport = mealReportService.createPendingMealReport(meal);
        log.info("사용자 id {}에 대한 mealReport가 생성되었습니다. reportid ={}", meal.getUser().getUserId(), mealReport.getReportId());
        User user = User.builder().userId(meal.getUser().getUserId()).build();

        eventPublisher.publishEvent(new MealSavedEvent(meal, user));

        return meal;
    }

    @Transactional
    public void updateMealWithAnalysis(AnalysisMealResultDto analysisMealResultDto) {
        mealService.updateMealWithAnalysis(analysisMealResultDto);
        foodService.createFoodsFromAnalysisAndLinkToMeal(analysisMealResultDto);
        mealReportService.updateMealReportWithAnalysis(analysisMealResultDto);
    }
}

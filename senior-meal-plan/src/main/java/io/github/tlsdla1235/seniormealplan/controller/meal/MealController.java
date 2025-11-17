package io.github.tlsdla1235.seniormealplan.controller.meal;

import io.github.tlsdla1235.seniormealplan.config.JwtAuthFilter;
import io.github.tlsdla1235.seniormealplan.domain.Meal;
import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.dto.meal.MealCachedDto;
import io.github.tlsdla1235.seniormealplan.dto.meal.MealCreateRequest;
import io.github.tlsdla1235.seniormealplan.dto.meal.MealResponseDto;
import io.github.tlsdla1235.seniormealplan.dto.s3dto.PresignedUrlRequest;
import io.github.tlsdla1235.seniormealplan.dto.s3dto.PresignedUrlResponse;
import io.github.tlsdla1235.seniormealplan.service.admin.S3UploadService;
import io.github.tlsdla1235.seniormealplan.service.food.MealService;
import io.github.tlsdla1235.seniormealplan.service.orchestration.UploadMealService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/user")
public class MealController {
    private final UploadMealService uploadMealService;
    private final MealService mealService;
    private final S3UploadService s3UploadService;

    @PostMapping("/me/uploads")
    public ResponseEntity<PresignedUrlResponse> generatePresignedUrl(@RequestBody PresignedUrlRequest request)
    {
        PresignedUrlResponse response = uploadMealService.generatePresignedUrl(request.fileName());
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/me/meal-reports")
    public ResponseEntity<String> generateMealReport(@RequestBody MealCreateRequest newMeal, @AuthenticationPrincipal JwtAuthFilter.JwtPrincipal me)
    {
        Meal meal = Meal.builder()
                .mealDate(newMeal.mealDate())
                .mealTime(newMeal.mealTime())
                .user(User.builder().userId(me.userId()).build())
                .memo(newMeal.memo())
                .mealType(newMeal.mealType())
                .build();
        uploadMealService.saveMeal(meal, newMeal.uniqueFileName());

        return ResponseEntity.ok().body("Meal report generated");
    }

    @GetMapping("/me/meals/today")
    public ResponseEntity<List<MealResponseDto>> getTodayMeals(@AuthenticationPrincipal JwtAuthFilter.JwtPrincipal me)
    {
        User user = User.builder().userId(me.userId()).build();
        List<MealCachedDto> cached= mealService.getTodayMeals(user);
        List<MealResponseDto> response = cached.stream()
                .map(c -> MealResponseDto.fromCached(
                        c, s3UploadService.generatePresignedUrlForGet(c.uniqueFileName())))
                .toList();

        return !response.isEmpty() ? ResponseEntity.ok().body(response) : ResponseEntity.noContent().build();
    }

    @GetMapping("/me/meals")
    public ResponseEntity<List<MealResponseDto>> getMealsByDate(
            @AuthenticationPrincipal JwtAuthFilter.JwtPrincipal me,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date)
    {
        User user = User.builder().userId(me.userId()).build();
        List<MealCachedDto> cached = mealService.getMealsByDate(user, date);
        List<MealResponseDto> response = cached.stream()
                .map(c -> MealResponseDto.fromCached(
                        c, s3UploadService.generatePresignedUrlForGet(c.uniqueFileName())))
                .toList();
        return !response.isEmpty()
                ? ResponseEntity.ok().body(response) // 리스트 전체 반환
                : ResponseEntity.noContent().build(); // 데이터가 없을 땐 204 No Content
    }

    @GetMapping("/me/meals/dates")
    public ResponseEntity<List<LocalDate>> getAllMealDates(@AuthenticationPrincipal JwtAuthFilter.JwtPrincipal me)
    {
        User user = User.builder().userId(me.userId()).build();
        List<LocalDate> dates = mealService.getAllMealDateFromUser(user);
        return !dates.isEmpty() ? ResponseEntity.ok().body(dates) : ResponseEntity.notFound().build();
    }


}

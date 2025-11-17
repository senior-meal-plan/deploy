package io.github.tlsdla1235.seniormealplan.controller.aiServerCon;


import io.github.tlsdla1235.seniormealplan.dto.meal.AnalysisMealRequestDto;
import io.github.tlsdla1235.seniormealplan.dto.meal.AnalysisMealResultDto;
import io.github.tlsdla1235.seniormealplan.dto.meal.MealDto;
import io.github.tlsdla1235.seniormealplan.service.orchestration.UploadMealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/webhooks/meals")
public class MealWebhookController {
    private final UploadMealService uploadMealService;

    @PostMapping("/analysis-result")
    public ResponseEntity<Void> receiveAnalysisResult(@RequestBody AnalysisMealResultDto result) {
        // 서비스 로직에 결과 처리를 위임
        uploadMealService.updateMealWithAnalysis(result);

        // 웹훅은 수신 확인 의미로 200 OK만 응답하는 것이 일반적
        return ResponseEntity.ok().build();
    }

    @PostMapping("/dtoCheckApi")
    public ResponseEntity<String> checkApi(@RequestBody AnalysisMealRequestDto dto) {
        return ResponseEntity.ok("good");
    }
}

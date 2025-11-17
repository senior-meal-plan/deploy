package io.github.tlsdla1235.seniormealplan.controller.report;

import io.github.tlsdla1235.seniormealplan.domain.Meal;
import io.github.tlsdla1235.seniormealplan.dto.mealreport.MealReportResponseDto;
import io.github.tlsdla1235.seniormealplan.service.report.MealReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/report")
@Slf4j
public class MealReportController {
    private final MealReportService mealReportService;


    @GetMapping("/meals/{mealId}")
    public ResponseEntity<MealReportResponseDto> getMealReportBymealId(@PathVariable Long mealId) {
        MealReportResponseDto response =  mealReportService.getMealReportByMealId(Meal.builder().mealId(mealId).build());
        return ResponseEntity.ok(response);
    }
}

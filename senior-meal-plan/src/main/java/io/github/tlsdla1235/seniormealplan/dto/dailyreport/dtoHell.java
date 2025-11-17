package io.github.tlsdla1235.seniormealplan.dto.dailyreport;

import io.github.tlsdla1235.seniormealplan.dto.meal.MealImageWithFoodNameDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record dtoHell(
        Long reportId,
        LocalDate date,
        BigDecimal summarize_score,
        BigDecimal basicScore,
        BigDecimal macularDegenerationScore,
        BigDecimal hypertensionScore,
        BigDecimal myocardialInfarctionScore,
        BigDecimal sarcopeniaScore,
        BigDecimal hyperlipidemiaScore,
        BigDecimal boneDiseaseScore,
        List<MealImageWithFoodNameDto> meals // 그날의 식사 이미지 목록
) {
}

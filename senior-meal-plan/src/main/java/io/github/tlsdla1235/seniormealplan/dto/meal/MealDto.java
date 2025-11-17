package io.github.tlsdla1235.seniormealplan.dto.meal;

import io.github.tlsdla1235.seniormealplan.domain.enumPackage.MealType;

import java.time.LocalTime;
import java.util.List;

public record MealDto(
        MealType mealType,
        LocalTime mealTime,
        String photoUrl,
        List<AnalyzedFoodDto> foods
) {
}

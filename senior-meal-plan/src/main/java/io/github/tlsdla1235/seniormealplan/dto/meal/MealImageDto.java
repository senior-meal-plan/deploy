package io.github.tlsdla1235.seniormealplan.dto.meal;

import io.github.tlsdla1235.seniormealplan.domain.Meal;

import java.time.LocalDate;

public record MealImageDto(
        Long mealId,
        String photoUrl,
        LocalDate date,
        String MealType
) {
    public static MealImageDto fromMeal(Meal meal, String presingedUrl) {
        return new MealImageDto(
                meal.getMealId(),
                presingedUrl,
                meal.getMealDate(),
                meal.getMealType().name()
        );
    }
}

package io.github.tlsdla1235.seniormealplan.dto.meal;

import io.github.tlsdla1235.seniormealplan.domain.Food;
import io.github.tlsdla1235.seniormealplan.domain.Meal;

import java.time.LocalDate;
import java.util.List;

public record MealImageWithFoodNameDto(
        Long mealId,
        String photoUrl,
        LocalDate date,
        String MealType,
        List<String> foodName
) {
    public static MealImageWithFoodNameDto fromMeal(Meal meal, String presingedUrl) {
        List<String> foodNames = meal.getFoods().stream()
                .map(Food::getName) // Food::getName
                .toList(); // .collect(Collectors.toList())
        return new MealImageWithFoodNameDto(
                meal.getMealId(),
                presingedUrl,
                meal.getMealDate(),
                meal.getMealType().name(),
                foodNames //
        );
    }
}

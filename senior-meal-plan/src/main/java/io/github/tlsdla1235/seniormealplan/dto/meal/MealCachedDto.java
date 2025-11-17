package io.github.tlsdla1235.seniormealplan.dto.meal;

import io.github.tlsdla1235.seniormealplan.domain.Food;
import io.github.tlsdla1235.seniormealplan.domain.Meal;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.MealType;

import java.io.Serializable;
import java.util.List;

public record MealCachedDto(
        Long mealId,
        String uniqueFileName,      // presignedUrl 생성에만 사용
        MealType mealType,
        List<String> foodNames
) implements Serializable {
    public static MealCachedDto from(Meal meal) {
        return new MealCachedDto(
                meal.getMealId(),
                meal.getUniqueFileName(),
                meal.getMealType(),
                meal.getFoods().stream().map(Food::getName).toList()
        );
    }
}

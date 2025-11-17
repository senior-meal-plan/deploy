package io.github.tlsdla1235.seniormealplan.dto.meal;

import io.github.tlsdla1235.seniormealplan.domain.Food;
import io.github.tlsdla1235.seniormealplan.domain.Meal;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.MealType;

import java.io.Serializable;
import java.util.List;

//User에게 MealReport를 보여주기 위해
public record MealResponseDto(
        Long mealId,
        String photoUrl,
        MealType mealType, // 아침(BREAKFAST), 점심(LUNCH), 저녁(DINNER) 구분
        List<String> foodNames // 해당 끼니에 포함된 음식 이름 목록
) implements Serializable {
    public static MealResponseDto from(Meal meal, String presignedPhotoUrl) {
        List<String> foodNames = meal.getFoods().stream()
                .map(Food::getName)
                .toList();

        return new MealResponseDto(
                meal.getMealId(),
                presignedPhotoUrl, // Meal의 photoUrl 대신 파라미터로 받은 URL 사용
                meal.getMealType(),
                foodNames
        );
    }

    public static MealResponseDto fromCached(MealCachedDto c, String presignedPhotoUrl) {
        return new MealResponseDto(c.mealId(), presignedPhotoUrl, c.mealType(), c.foodNames());
    }


}

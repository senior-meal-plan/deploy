package io.github.tlsdla1235.seniormealplan.dto.weeklyreport;

import io.github.tlsdla1235.seniormealplan.domain.Meal;
import io.github.tlsdla1235.seniormealplan.dto.meal.AnalyzedFoodDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record MealForWeeklyDto(
        Long mealId,           // 어떤 식단에 대한 분석 결과인지 식별자
        LocalDate mealDate,    // (추가) 식사 날짜
        LocalTime mealTime,
        String mealType,
        BigDecimal totalKcal,  // 분석된 총 칼로리 (Meal 엔티티 값)
        BigDecimal totalProtein, // (Meal 엔티티 값)
        BigDecimal totalCarbs,   // (Meal 엔티티 값)
        BigDecimal totalFat,     // (Meal 엔티티 값)
        BigDecimal totalCalcium, // (Meal 엔티티 값)
        List<FoodForWeeklyDto> foods
) {
    public static MealForWeeklyDto fromMeal(Meal meal) {
        List<FoodForWeeklyDto> foodDtos = (meal.getFoods() == null)
                ? Collections.emptyList()
                : meal.getFoods().stream()
                .map(FoodForWeeklyDto::from)
                .collect(Collectors.toList());

        return new MealForWeeklyDto(
                meal.getMealId(),
                meal.getMealDate(),
                meal.getMealTime(),
                meal.getMealType().name(),
                meal.getTotalKcal(),
                meal.getTotalProtein(),
                meal.getTotalCarbs(),
                meal.getTotalFat(),
                meal.getTotalCalcium(),
                foodDtos // 완성된 food DTO 리스트 전달
        );
    }
}

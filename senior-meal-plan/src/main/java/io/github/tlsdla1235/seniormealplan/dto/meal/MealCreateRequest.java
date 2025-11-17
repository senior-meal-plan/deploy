package io.github.tlsdla1235.seniormealplan.dto.meal;

import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.MealType;

import java.time.LocalDate;
import java.time.LocalTime;

public record MealCreateRequest(
                                MealType mealType,
                                LocalDate mealDate,
                                LocalTime mealTime,
                                String memo,
                                String uniqueFileName) {
}

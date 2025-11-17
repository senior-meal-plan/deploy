package io.github.tlsdla1235.seniormealplan.dto.async;

import io.github.tlsdla1235.seniormealplan.domain.Meal;
import io.github.tlsdla1235.seniormealplan.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MealSavedEvent {
    private final Meal meal;
    private final User user;
}

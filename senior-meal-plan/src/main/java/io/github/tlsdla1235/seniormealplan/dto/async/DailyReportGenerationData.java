package io.github.tlsdla1235.seniormealplan.dto.async;


import io.github.tlsdla1235.seniormealplan.domain.Meal;
import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.report.DailyReport;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class DailyReportGenerationData {
    private final User user;
    private final List<Meal> meals;
    private final DailyReport report;
}

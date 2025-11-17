package io.github.tlsdla1235.seniormealplan.dto.async;


import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.report.WeeklyReport;
import io.github.tlsdla1235.seniormealplan.dto.user.WhoAmIDto;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.DailyReportsForWeeklyReportDto;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.MealForWeeklyDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@RequiredArgsConstructor
@ToString
public class WeeklyReportGenerationData {
    private final User user;
    private final WeeklyReport report;
    private final WhoAmIDto userDto;
    private final List<DailyReportsForWeeklyReportDto> dailyReportDto;
    private final List<MealForWeeklyDto> mealsDto;
}

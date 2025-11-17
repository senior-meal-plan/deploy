package io.github.tlsdla1235.seniormealplan.dto.weeklyreport;

import io.github.tlsdla1235.seniormealplan.dto.meal.MealResponseDto;
import io.github.tlsdla1235.seniormealplan.dto.user.WhoAmIDto;

import java.util.List;

public record WeeklyReportGenerateRequestDto(
        Long weeklyReportId,
        WhoAmIDto userDto,
        List<DailyReportsForWeeklyReportDto> dailyReports,
        List<MealForWeeklyDto> meals
) {
}

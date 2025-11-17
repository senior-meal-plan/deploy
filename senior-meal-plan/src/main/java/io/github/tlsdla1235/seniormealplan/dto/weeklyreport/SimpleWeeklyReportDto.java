package io.github.tlsdla1235.seniormealplan.dto.weeklyreport;

import java.time.LocalDate;

public record SimpleWeeklyReportDto(
        Long reportId,
        LocalDate weekStart,
        LocalDate weekEnd
) {
}
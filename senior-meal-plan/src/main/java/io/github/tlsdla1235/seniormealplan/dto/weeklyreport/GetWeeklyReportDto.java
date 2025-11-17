package io.github.tlsdla1235.seniormealplan.dto.weeklyreport;

import io.github.tlsdla1235.seniormealplan.domain.report.WeeklyReport;

import java.time.LocalDate;

public record GetWeeklyReportDto(
        Long WeeklyReportId,
        LocalDate weekStart,
        LocalDate weekEnd,
        String summaryGoodPoint,
        String summaryBadPoint,
        String summaryAiRecommand,
        String severity
) {
    public static GetWeeklyReportDto toDto(WeeklyReport weeklyReport) {
        return new GetWeeklyReportDto(
                weeklyReport.getReportId(),
                weeklyReport.getWeekStart(),
                weeklyReport.getWeekEnd(),
                weeklyReport.getSummaryGoodPoint(),
                weeklyReport.getSummaryBadPoint(),
                weeklyReport.getSummaryAiRecommand(),
                weeklyReport.getSeverity().name()
        );
    }
}

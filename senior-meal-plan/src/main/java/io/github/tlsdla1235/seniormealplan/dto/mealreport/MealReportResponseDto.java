package io.github.tlsdla1235.seniormealplan.dto.mealreport;

import io.github.tlsdla1235.seniormealplan.domain.report.MealReport;

public record MealReportResponseDto(
        Long mealId,
        String severity,
        String summary
) {
    public static MealReportResponseDto fromMealReport(MealReport mealReport) {
        return new MealReportResponseDto(mealReport.getMeal().getMealId(),mealReport.getSeverity().name(),mealReport.getSummary());
    }
}

package io.github.tlsdla1235.seniormealplan.dto.weeklyreport.AnalysisResultDto;

import java.util.List;

public record WeeklyAnalysisResultDto(
        Long UserId,
    AnalysisWeeklyReportDto weeklyReport,
    List<String> aiRecommendTopic,
    List<Long> aiRecommendRecipe
) {
}
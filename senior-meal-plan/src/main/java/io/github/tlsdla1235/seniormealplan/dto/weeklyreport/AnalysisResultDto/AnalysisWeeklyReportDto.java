package io.github.tlsdla1235.seniormealplan.dto.weeklyreport.AnalysisResultDto;

public record AnalysisWeeklyReportDto(
        Long WeeklyReportId,
        String summaryGoodPoint,
        String summaryBadPoint,
        String summaryAiRecommend,
        String severity
) {
}

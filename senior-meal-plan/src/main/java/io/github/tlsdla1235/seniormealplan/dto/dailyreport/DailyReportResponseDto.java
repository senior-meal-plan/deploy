package io.github.tlsdla1235.seniormealplan.dto.dailyreport;

import io.github.tlsdla1235.seniormealplan.domain.report.DailyReport;

import java.math.BigDecimal;

public record DailyReportResponseDto(
        String severity,
        String summary,
        BigDecimal summarize_score,
        BigDecimal basicScore,
        BigDecimal macularDegenerationScore,
        BigDecimal hypertensionScore,
        BigDecimal myocardialInfarctionScore,
        BigDecimal sarcopeniaScore,
        BigDecimal hyperlipidemiaScore,
        BigDecimal boneDiseaseScore

        ) {
    public static DailyReportResponseDto fromDailyReport(DailyReport dailyReport)
    {
        return new DailyReportResponseDto(
                dailyReport.getSeverity().name(),
                dailyReport.getSummary(),
                dailyReport.getSummarizeScore(),
                dailyReport.getBasicScore(),
                dailyReport.getMacularDegenerationScore(),
                dailyReport.getHypertensionScore(),
                dailyReport.getMyocardialInfarctionScore(),
                dailyReport.getSarcopeniaScore(),
                dailyReport.getHyperlipidemiaScore(),
                dailyReport.getBoneDiseaseScore()
                );
    }
}

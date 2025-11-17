package io.github.tlsdla1235.seniormealplan.dto.dailyreport;

import io.github.tlsdla1235.seniormealplan.domain.report.DailyReport;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyReportForWeeklyDto(
        Long ReportId,
        LocalDate date,
        BigDecimal summarizeScore,
        BigDecimal basicScore,
        BigDecimal macularDegenerationScore,
        BigDecimal hypertensionScore,
        BigDecimal myocardialInfarctionScore,
        BigDecimal sarcopeniaScore,
        BigDecimal hyperlipidemiaScore,
        BigDecimal boneDiseaseScore
) {
    public static DailyReportForWeeklyDto from(DailyReport dailyReport) {
        return new DailyReportForWeeklyDto(
                dailyReport.getReportId(),
                dailyReport.getReportDate(),
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

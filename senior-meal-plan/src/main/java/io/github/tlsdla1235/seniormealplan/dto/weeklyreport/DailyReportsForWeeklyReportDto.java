package io.github.tlsdla1235.seniormealplan.dto.weeklyreport;

import io.github.tlsdla1235.seniormealplan.domain.enumPackage.ReportStatus;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.Severity;
import io.github.tlsdla1235.seniormealplan.domain.report.DailyReport;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyReportsForWeeklyReportDto(
        Long reportId,
        Long userId,
        LocalDate reportDate,
        ReportStatus status,

        // DailyReport 클래스의 필드
        BigDecimal totalKcal,
        BigDecimal totalProtein,
        BigDecimal totalCarbs,
        BigDecimal totalFat,
        BigDecimal totalCalcium,
        String summary,
        Severity severity,

        // 점수 필드
        BigDecimal summarizeScore,
        BigDecimal basicScore,
        BigDecimal macularDegenerationScore,
        BigDecimal hypertensionScore,
        BigDecimal myocardialInfarctionScore,
        BigDecimal sarcopeniaScore,
        BigDecimal hyperlipidemiaScore,
        BigDecimal boneDiseaseScore
) {
    public static DailyReportsForWeeklyReportDto from(DailyReport dailyReport) {
        // 상태가 COMPLETE가 아니거나 객체가 null이면 null을 반환합니다.
        if (dailyReport == null || dailyReport.getStatus() != ReportStatus.COMPLETE) {
            return null;
        }

        return new DailyReportsForWeeklyReportDto(
                dailyReport.getReportId(),
                dailyReport.getUser().getUserId(),
                dailyReport.getReportDate(),
                dailyReport.getStatus(),
                dailyReport.getTotalKcal(),
                dailyReport.getTotalProtein(),
                dailyReport.getTotalCarbs(),
                dailyReport.getTotalFat(),
                dailyReport.getTotalCalcium(),
                dailyReport.getSummary(),
                dailyReport.getSeverity(),
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

package io.github.tlsdla1235.seniormealplan.dto.dailyreport;

import java.math.BigDecimal;

public record DailyReportAnalysisResultDto(
        Long reportId,         // 어떤 리포트에 대한 결과인지 식별자
        String status,         // "SUCCESS" 또는 "FAILURE"
        String errorMessage,   // 실패 시 에러 메시지 (선택적)

        // DailyReport의 필드들
        BigDecimal totalKcal,
        BigDecimal totalProtein,
        BigDecimal totalCarbs,
        BigDecimal totalFat,
        BigDecimal totalCalcium,
        String summary,
        String severity,       // Enum으로 바로 받기보다 String으로 받아 안전하게 변환

        BigDecimal summarizeScore,
        BigDecimal basicScore,
        BigDecimal macularDegenerationScore,
        BigDecimal hypertensionScore,
        BigDecimal myocardialInfarctionScore,
        BigDecimal sarcopeniaScore,
        BigDecimal hyperlipidemiaScore,
        BigDecimal boneDiseaseScore

) {
}

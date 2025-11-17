package io.github.tlsdla1235.seniormealplan.domain.report;

import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.ReportStatus;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.Severity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("DAILY") // report_type이 'DAILY'인 경우
public class DailyReport extends Report {

    @Column(name = "total_kcal")
    private BigDecimal totalKcal;

    @Column(name = "total_protein")
    private BigDecimal totalProtein;

    @Column(name = "total_carbs")
    private BigDecimal totalCarbs;

    @Column(name = "total_fat")
    private BigDecimal totalFat;

    @Column(name = "total_calcium")
    private BigDecimal totalCalcium;

    @Lob
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity")
    private Severity severity;

    @Column(name = "summarize_score")
    private BigDecimal summarizeScore;

    @Column(name = "basic_score")
    private BigDecimal basicScore;

        @Column(name = "macular_degeneration_score")
        private BigDecimal macularDegenerationScore; // 황반변성 점수

        @Column(name = "hypertension_score")
        private BigDecimal hypertensionScore; // 고혈압 점수

        @Column(name = "myocardial_infarction_score")
        private BigDecimal myocardialInfarctionScore; // 심근경색 점수

        @Column(name = "sarcopenia_score")
        private BigDecimal sarcopeniaScore; // 근감소증 점수

        @Column(name = "hyperlipidemia_score")
        private BigDecimal hyperlipidemiaScore; // 고지혈증 점수

        @Column(name = "bone_disease_score")
        private BigDecimal boneDiseaseScore; // 뼈질환 점수


    public DailyReport(User user, LocalDate reportDate) {
        super.setUser(user);
        super.setReportDate(reportDate);
        // status는 @PrePersist에 의해 PENDING으로 자동 설정됩니다.
    }

    public void updateWithAnalysis(BigDecimal totalKcal, BigDecimal totalProtein,
                                   BigDecimal totalCarbs, BigDecimal totalFat,
                                   BigDecimal totalCalcium, String summary,
                                   Severity severity, BigDecimal summarizeScore, BigDecimal basicScore,
                                   BigDecimal macularDegenerationScore,
                                   BigDecimal hyperlipidemiaScore,
                                   BigDecimal myocardialInfarctionScore,
                                   BigDecimal sarcopeniaScore,
                                   BigDecimal boneDiseaseScore,
                                   BigDecimal hypertensionScore
                                   ) {
        this.totalKcal = totalKcal;
        this.totalProtein = totalProtein;
        this.totalCarbs = totalCarbs;
        this.totalFat = totalFat;
        this.totalCalcium = totalCalcium;
        this.summary = summary;
        this.severity = severity;
        this.summarizeScore = summarizeScore;
        this.macularDegenerationScore = macularDegenerationScore;
        this.hypertensionScore = hypertensionScore;
        this.myocardialInfarctionScore = myocardialInfarctionScore;
        this.sarcopeniaScore = sarcopeniaScore;
        this.hyperlipidemiaScore = hyperlipidemiaScore;
        this.boneDiseaseScore = boneDiseaseScore;
        this.basicScore = basicScore;
        super.changeStatus(ReportStatus.COMPLETE); // 상태를 COMPLETE로 변경
    }

    /**
     * 분석 실패 시 상태를 FAILED로 변경합니다.
     */
    public void markAsFailed() {
        super.changeStatus(ReportStatus.FAILED);
    }
}
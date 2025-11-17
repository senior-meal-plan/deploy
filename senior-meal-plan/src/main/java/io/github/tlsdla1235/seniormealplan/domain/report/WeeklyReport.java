package io.github.tlsdla1235.seniormealplan.domain.report;


import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.ReportStatus;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.Severity;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.GetWeeklyReportDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "weekly_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("WEEKLY") // report_type이 'WEEKLY'인 경우
public class WeeklyReport extends Report {

    @Column(name = "week_start")
    private LocalDate weekStart;

    @Column(name = "week_end")
    private LocalDate weekEnd;

    @Lob
    @Column(name = "summary_good_point", columnDefinition = "TEXT")
    private String summaryGoodPoint;

    @Lob
    @Column(name = "summary_bad_point", columnDefinition = "TEXT")
    private String summaryBadPoint;

    @Lob
    @Column(name = "summary_ai_recommand", columnDefinition = "TEXT")
    private String summaryAiRecommand;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    public WeeklyReport(User user, LocalDate weekStart, LocalDate weekEnd) {
        super.setUser(user);
        super.setReportDate(weekEnd);
        this.weekStart = weekStart;
        this.weekEnd = weekEnd;
    }

    public void UpdateWithAnalysis(String summaryGoodPoint, String summaryBadPoint, String summaryAiRecommend, Severity severity) {
        this.summaryGoodPoint = summaryGoodPoint;
        this.summaryBadPoint = summaryBadPoint;
        this.summaryAiRecommand = summaryAiRecommend;
        this.severity = severity;
        super.changeStatus(ReportStatus.COMPLETE);
    }

}
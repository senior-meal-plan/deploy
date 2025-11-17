package io.github.tlsdla1235.seniormealplan.domain.report;

import io.github.tlsdla1235.seniormealplan.domain.Meal;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.Severity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "meal_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("MEAL") // report_type이 'MEAL'인 경우
public class MealReport extends Report {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id")
    private Meal meal;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    public MealReport(Meal meal) {
        if (meal == null || meal.getUser() == null) {
            throw new IllegalArgumentException("Meal and its User cannot be null");
        }
        this.meal = meal;
        // 부모(Report)의 필드 설정
        super.setUser(meal.getUser());
        super.setReportDate(meal.getMealDate());
    }

    public void updateWithAnalysis(String summary, Severity severity) {
        this.summary = summary;
        this.severity = severity;
    }
}
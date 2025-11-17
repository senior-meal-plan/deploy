package io.github.tlsdla1235.seniormealplan.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metric_id")
    private Long metricId;

    @OneToOne(fetch = FetchType.LAZY) // User(1) : HealthMetric(1) 관계
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "bmi")
    private BigDecimal bmi;

    @Column(name = "daily_kcal")
    private BigDecimal dailyKcal;

    @Column(name = "daily_protein")
    private BigDecimal dailyProtein;

    @Column(name = "daily_fat")
    private BigDecimal dailyFat;

    @Column(name = "daily_calcium")
    private BigDecimal dailyCalcium;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;

    @PrePersist
    public void prePersist() {
        this.calculatedAt = LocalDateTime.now();
    }
}

//이게 위클리 리포트를 작성할 떄, 작성될 수 있는지
//이게 작성된다면, 어떤식으로 작용될지에 대해서 여쭤보고 싶었어요. + 위클리리포트나 데일리 리포트를 작성하실떄
//제가 이 데이터를 보내줄수 있어요
//그걸 이제 참고해서 레포트를 작성하는식으로




package io.github.tlsdla1235.seniormealplan.domain;


import io.github.tlsdla1235.seniormealplan.domain.enumPackage.Role;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.UserGenderType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="users")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;


    @Column(name = "user_input_id" , nullable = false, unique = true)
    private String userInputId;

    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Role role = Role.USER;

    @Column(name = "user_name", nullable = false) // nullable=false는 NOT NULL 제약조건
    private String userName;

    @Column(name= "age", nullable = false)
    private Integer age;

    @Column(name = "created_at", updatable = false) // updatable=false는 한번 생성되면 수정되지 않음
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING) // Enum 타입을 문자열 자체로 DB에 저장
    @Column(name = "user_gender")
    private UserGenderType userGender;

    @Column(name = "user_height")
    private BigDecimal userHeight; // 정밀한 소수점 계산을 위해 BigDecimal 사용

    @Column(name = "user_weight")
    private BigDecimal userWeight;

    @Column(name = "user_last_login")
    private LocalDateTime userLastLogin;

    @Column(name = "last_daily_report_date")
    private LocalDate lastDailyReportDate; // 마지막 데일리 리포트 작성일

    @Column(name = "last_weekly_report_date")
    private LocalDate lastWeeklyReportDate; // 마지막 위클리 리포트 작성일

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default // Lombok Builder 사용 시 이 필드를 누락하면 자동으로 기본값을 설정해줌
    private boolean isDeleted = false; // 필드 선언 시 기본값 false로 초기화

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Meal> meals = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.role == null) this.role = Role.USER;
    }

    public void updateProfile(String userName, Integer userAge, BigDecimal userHeight,
                              BigDecimal userWeight, UserGenderType userGender) {
        this.userName = userName;
        this.age = userAge;
        this.userHeight = userHeight;
        this.userWeight = userWeight;
        this.userGender = userGender;
    }
}


package io.github.tlsdla1235.seniormealplan.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.MealType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meals")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meal_id")
    private Long mealId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @OneToMany(mappedBy = "meal", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("meal-food")
    private List<Food> foods = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false)
    private MealType mealType;

    @Column(name = "meal_date")
    private LocalDate mealDate;

    @Column(name = "meal_time")
    private LocalTime mealTime;

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

    @Lob // 글자 수 제한이 없는 TEXT 타입과 매핑
    @Column(name = "memo")
    private String memo;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "unique_file_name")
    private String uniqueFileName;

    @Column(name = "is_dairy_intake", nullable = false)
    @Builder.Default
    private boolean isDairyIntake = false;

    @Column(name = "is_vitamin_c_intake", nullable = false)
    @Builder.Default
    private boolean isVitaminCIntake = false;

    @Column(name = "is_vitamin_b_intake", nullable = false)
    @Builder.Default
    private boolean isVitaminBIntake = false;

    @Column(name = "is_fish_intake", nullable = false)
    @Builder.Default
    private boolean isFishIntake = false;

    @Column(name = "is_nuts_intake", nullable = false)
    @Builder.Default
    private boolean isNutsIntake = false;

    @Column(name = "is_vegetable_oil_intake", nullable = false)
    @Builder.Default
    private boolean isVegetableOilIntake = false;

    @Column(name = "is_unrefined_carbs_intake", nullable = false)
    @Builder.Default
    private boolean isUnrefinedCarbsIntake = false;



    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default // Lombok Builder 사용 시 이 필드를 누락하면 자동으로 기본값을 설정해줌
    private boolean isDeleted = false; // 필드 선언 시 기본값 false로 초기화

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
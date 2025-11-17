package io.github.tlsdla1235.seniormealplan.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "foods")
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_id")
    private Long foodId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "kcal")
    private BigDecimal kcal;

    @Column(name = "protein")
    private BigDecimal protein;

    @Column(name = "carbs")
    private BigDecimal carbs;

    @Column(name = "fat")
    private BigDecimal fat;

    @Column(name = "calcium")
    private BigDecimal calcium;

    @Column(name = "serving_size")
    private BigDecimal servingSize;

    //------추가 된 컬럼-----

    @Column(name = "saturated_fat_percent_kcal")
    private BigDecimal saturatedFatPercentKcal; // 포화지방(%kcal)

    @Column(name = "unsaturated_fat_g")
    private BigDecimal unsaturatedFat; // 불포화지방(g)

    @Column(name = "dietary_fiber_g")
    private BigDecimal dietaryFiber; // 식이섬유(g)

    @Column(name = "sodium_mg")
    private BigDecimal sodium; // 나트륨(mg)

    @Column(name = "added_sugar_kcal")
    private BigDecimal addedSugarKcal; // 첨가당(kcal)

    @Column(name = "is_fried", nullable = false)
    @Builder.Default
    private boolean isFried = false; // 튀김 섭취(Bool)

    @Column(name = "processed_meat_gram")
    private BigDecimal processedMeatGram; // 가공육(g)

    @Column(name = "vitamin_d_iu")
    private BigDecimal vitaminD_IU; // 비타민 D(UI)

    @Column(name = "is_vegetable", nullable = false)
    @Builder.Default
    private boolean isVegetable = false; // 채소 섭취(bool)

    @Column(name = "is_fruit", nullable = false)
    @Builder.Default
    private boolean isFruit = false; // 과일 섭취(bool)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id")
    @JsonBackReference("meal-food")
    private Meal meal;
}

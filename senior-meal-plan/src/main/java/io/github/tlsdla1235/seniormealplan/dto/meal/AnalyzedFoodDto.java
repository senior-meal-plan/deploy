package io.github.tlsdla1235.seniormealplan.dto.meal;

import java.math.BigDecimal;

public record AnalyzedFoodDto(
        String name,           // 분석된 음식 이름
        BigDecimal kcal,         // 칼로리
        BigDecimal protein,      // 단백질
        BigDecimal carbs,        // 탄수화물
        BigDecimal fat,          // 지방
        BigDecimal calcium,      // 칼슘
        BigDecimal servingSize,   // 섭취량 (예: 1.0인분, 0.5인분)
        
        //추가된 필드
        
        BigDecimal saturatedFatPercentKcal, // 포화지방(%kcal)
        BigDecimal unsaturatedFat,          // 불포화지방(g)
        BigDecimal dietaryFiber,            // 식이섬유(g)
        BigDecimal sodium,                  // 나트륨(mg)
        BigDecimal addedSugarKcal,          // 첨가당(kcal)
        BigDecimal processedMeatGram,       // 가공육(g)
        BigDecimal vitaminD_IU,             // 비타민 D(UI)
        boolean isVegetable,                // 채소 섭취(bool)
        boolean isFruit,                     // 과일 섭취(bool)
        boolean isFried                     // 튀김 섭취(Bool)
) {
}

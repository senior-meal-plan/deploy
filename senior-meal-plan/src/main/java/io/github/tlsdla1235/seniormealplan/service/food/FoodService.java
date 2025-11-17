package io.github.tlsdla1235.seniormealplan.service.food;

import io.github.tlsdla1235.seniormealplan.domain.Food;
import io.github.tlsdla1235.seniormealplan.domain.Meal;
import io.github.tlsdla1235.seniormealplan.dto.meal.AnalysisMealResultDto;
import io.github.tlsdla1235.seniormealplan.dto.meal.AnalyzedFoodDto;
import io.github.tlsdla1235.seniormealplan.repository.FoodRepository;
import io.github.tlsdla1235.seniormealplan.repository.MealRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FoodService {
    private final FoodRepository foodRepository;
    private final MealRepository mealRepository;


    @Transactional // <-- 트랜잭션 추가
    @Caching(evict = {
            @CacheEvict(value = "todayMeals",
                    key = "#result.user.userId",
                    condition = "#result.mealDate.isEqual(T(java.time.LocalDate).now())"),
            @CacheEvict(value = "mealsByDate",
                    key = "#result.user.userId + '_' + #result.mealDate.toString()")
    })
    public Meal createFoodsFromAnalysisAndLinkToMeal(AnalysisMealResultDto resultDto) {
        Meal meal = mealRepository.findById(resultDto.mealId())
                .orElseThrow(() -> new EntityNotFoundException("Meal not found with id: " + resultDto.mealId()));

        meal.getFoods().clear();

        if (resultDto.foods() == null || resultDto.foods().isEmpty()) {
            log.warn("No food items found in analysis result for Meal ID: {}", resultDto.mealId());
            return meal; // ← 반환해야 @CacheEvict의 #result 사용 가능
        }

        for (AnalyzedFoodDto foodDto : resultDto.foods()) {
            Food newFood = Food.builder()
                    .meal(meal)
                    .name(foodDto.name())
                    .kcal(foodDto.kcal())
                    .protein(foodDto.protein())
                    .carbs(foodDto.carbs())
                    .fat(foodDto.fat())
                    .calcium(foodDto.calcium())
                    .servingSize(foodDto.servingSize())
                    .saturatedFatPercentKcal(foodDto.saturatedFatPercentKcal())
                    .unsaturatedFat(foodDto.unsaturatedFat())
                    .dietaryFiber(foodDto.dietaryFiber())
                    .sodium(foodDto.sodium())
                    .addedSugarKcal(foodDto.addedSugarKcal())
                    .processedMeatGram(foodDto.processedMeatGram())
                    .vitaminD_IU(foodDto.vitaminD_IU())
                    .isVegetable(foodDto.isVegetable())
                    .isFruit(foodDto.isFruit())
                    .isFried(foodDto.isFried())
                    .build();
            meal.getFoods().add(newFood);
        }
        log.info("{} food items created and linked to Meal ID: {}", meal.getFoods().size(), meal.getMealId());
        return meal; // ★ 중요
    }


}

package io.github.tlsdla1235.seniormealplan.repository.recipe;

import io.github.tlsdla1235.seniormealplan.domain.recipe.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

//    @Query(
//            "SELECT r.recipeId " +
//                    "FROM Recipe r " +
//                    "JOIN r.recipeTypes rrt_goal " +
//                    "JOIN rrt_goal.recipeType rt_goal " +
//                    "LEFT JOIN r.recipeTypes rrt_allergy " +
//                    "LEFT JOIN rrt_allergy.recipeType rt_allergy " +
//                    "WHERE rt_goal.typeName IN :healthGoalNames " +
//                    "GROUP BY r.recipeId " +
//                    "HAVING SUM(CASE WHEN rt_allergy.typeName IN :allergenNames THEN 1 ELSE 0 END) = 0 " +
//                    "ORDER BY r.recipeId ASC"
//    )
//    List<Long> findRecommendedRecipeIdsByGoalsExcludingAllergens(
//            @Param("healthGoalNames") List<String> healthGoalNames,
//            @Param("allergenNames") List<String> allergenNames,
//            Pageable pageable
//    );

    @Query("""
SELECT r.recipeId
FROM Recipe r
WHERE 
  EXISTS (
    SELECT 1
    FROM r.recipeTypes rrt_goal
    JOIN rrt_goal.recipeType rt_goal
    WHERE rt_goal.typeName IN :healthGoalNames
  )
  AND NOT EXISTS (
    SELECT 1
    FROM r.recipeTypes rrt_allergy
    JOIN rrt_allergy.recipeType rt_allergy
    WHERE rt_allergy.typeName IN :allergenNames
  )
ORDER BY r.recipeId ASC
""")
    List<Long> findRecommendedRecipeIdsByGoalsExcludingAllergens_AnyGoal(
            @Param("healthGoalNames") List<String> healthGoalNames,
            @Param("allergenNames") List<String> allergenNames,
            Pageable pageable
    );


    /**
     * CASE 1-B: [건강 목표 O, 알러젠 X]
     * - 목표만 적용 (알러젠 목록이 비었을 때 호출)
     */
    @Query("""
SELECT r.recipeId
FROM Recipe r
WHERE EXISTS (
  SELECT 1
  FROM r.recipeTypes rrt_goal
  JOIN rrt_goal.recipeType rt_goal
  WHERE rt_goal.typeName IN :healthGoalNames
)
ORDER BY r.recipeId ASC
""")
    List<Long> findRecommendedRecipeIdsByGoalsOnly_AnyGoal(
            @Param("healthGoalNames") List<String> healthGoalNames,
            Pageable pageable
    );

    /**
     * CASE 2-A: [건강 목표 X, 알러젠 O]
     * - 선택한 알러젠을 포함하지 않는 레시피
     */
    @Query("""
SELECT r.recipeId
FROM Recipe r
WHERE NOT EXISTS (
  SELECT 1
  FROM r.recipeTypes rrt_allergy
  JOIN rrt_allergy.recipeType rt_allergy
  WHERE rt_allergy.typeName IN :allergenNames
)
ORDER BY r.recipeId ASC
""")
    List<Long> findTopRecipeIdsExcludingAllergens_NotExists(
            @Param("allergenNames") List<String> allergenNames,
            Pageable pageable
    );

    /**
     * CASE 2-B: [건강 목표 X, 알러젠 X]
     * - 알러젠 조건도 없으면 전체에서 상위 N (정렬 기준은 동일)
     */
    @Query(
            "SELECT r.recipeId " +
                    "FROM Recipe r " +
                    "GROUP BY r.recipeId " +
                    "ORDER BY r.recipeId ASC"
    )
    List<Long> findTopRecipeIdsNoFilters(Pageable pageable);
}

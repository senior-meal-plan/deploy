package io.github.tlsdla1235.seniormealplan.repository.recipe;

import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.recipe.Recipe;
import io.github.tlsdla1235.seniormealplan.domain.recipe.UserWeeklyRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface UserWeeklyRecommendationRepository extends JpaRepository<UserWeeklyRecommendation, Long> {
    @Query("SELECT r FROM UserWeeklyRecommendation r " +
            "WHERE r.user = :user AND r.generatedAt = " +
            "(SELECT MAX(r2.generatedAt) FROM UserWeeklyRecommendation r2 WHERE r2.user = :user)")
    List<UserWeeklyRecommendation> findMostRecentByUser(@Param("user") User user);

    @Query("SELECT uwr.recipe FROM UserWeeklyRecommendation uwr " +
            "JOIN uwr.recipe " + // 내부적으로 조인 수행
            "WHERE uwr.user.userId = :userId " +
            "AND uwr.generatedAt BETWEEN :startDate AND :endDate")
    List<Recipe> findRecipesByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}

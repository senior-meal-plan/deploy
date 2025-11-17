package io.github.tlsdla1235.seniormealplan.repository;

import io.github.tlsdla1235.seniormealplan.domain.Meal;
import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.dto.meal.AnalyzedFoodDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface MealRepository extends JpaRepository<Meal, Long> {
    @Query("SELECT m FROM Meal m LEFT JOIN FETCH m.foods WHERE m.user = :user AND m.mealDate = :date")
    List<Meal> findByUserAndMealDateWithFoods(@Param("user") User user, @Param("date") LocalDate date);

    @Query("SELECT DISTINCT m.mealDate FROM Meal m WHERE m.user = :user ORDER BY m.mealDate DESC")
    List<LocalDate> findDistinctMealDatesByUser(@Param("user") User user);

    @Query("SELECT m FROM Meal m JOIN FETCH m.foods f WHERE m.user = :user AND m.mealDate BETWEEN :startDate AND :endDate ORDER BY m.mealDate, m.mealType")
    List<Meal> findByUserAndMealDateBetweenWithFoods(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT m FROM Meal m LEFT JOIN FETCH m.foods f WHERE m.user = :user AND m.mealDate IN :dates")
    List<Meal> findByUserAndMealDateIn(User user, Collection<LocalDate> dates);

    @Query("SELECT DISTINCT m.user FROM Meal m WHERE m.mealDate = :date")
    List<User> findDistinctUsersByMealDate(@Param("date") LocalDate date);

    @Query("SELECT DISTINCT m.user FROM Meal m WHERE m.mealDate BETWEEN :startDate AND :endDate")
    List<User> findDistinctUsersByMealDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}

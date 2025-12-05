package io.github.tlsdla1235.seniormealplan.repository;

import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.MealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserInputId(String userInputId);
    boolean existsByUserInputId(String userInputId);

    @Query("SELECT u.fcmToken FROM User u " +
            "WHERE u.fcmToken IS NOT NULL " +
            "AND u.fcmToken != '' " +
            "AND NOT EXISTS (" +
            "   SELECT m FROM Meal m " +
            "   WHERE m.user = u " +
            "   AND m.mealDate = :today " +
            "   AND m.mealType = :mealType" +
            ")")
    List<String> findTokensByNoMealLog(@Param("today") LocalDate today,
                                       @Param("mealType") MealType mealType);
}

package io.github.tlsdla1235.seniormealplan.repository;

import io.github.tlsdla1235.seniormealplan.domain.report.MealReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MealReportRepository extends JpaRepository<MealReport, Long> {
    Optional<MealReport> findByMeal_MealId(Long mealId);
}

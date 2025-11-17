package io.github.tlsdla1235.seniormealplan.repository;

import io.github.tlsdla1235.seniormealplan.domain.HealthMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthMetricRepository extends JpaRepository<HealthMetric, Long> {
}

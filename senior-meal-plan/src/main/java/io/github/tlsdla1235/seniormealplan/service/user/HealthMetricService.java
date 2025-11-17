package io.github.tlsdla1235.seniormealplan.service.user;

import io.github.tlsdla1235.seniormealplan.domain.HealthMetric;
import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.UserGenderType;
import io.github.tlsdla1235.seniormealplan.repository.HealthMetricRepository;
import jakarta.persistence.Column;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;


@Service
@Slf4j
@RequiredArgsConstructor
public class HealthMetricService {
    private final HealthMetricRepository healthMetricRepository;


    @Transactional
    public void init_HealthMetricService(User user) {
        HealthMetric healthMetric = makeMetricByBasicInput(user);
        healthMetricRepository.save(healthMetric);
        log.info("사용자 {}에 대한 초기 metric 생성, metricId: {}", user.getUserName(), healthMetric.getMetricId());
    }


    public HealthMetric makeMetricByBasicInput(User user) {
        int age = user.getAge();
        BigDecimal height = user.getUserHeight();
        BigDecimal weight = user.getUserWeight();
        UserGenderType gender = user.getUserGender();

        BigDecimal bmr;
        BigDecimal activityFactor = new BigDecimal("1.2");
        BigDecimal ageDecimal = new BigDecimal(age);

        BigDecimal term1 = new BigDecimal("10").multiply(weight);
        BigDecimal term2 = new BigDecimal("6.25").multiply(height);
        BigDecimal term3 = new BigDecimal("5").multiply(ageDecimal);

        if (gender == UserGenderType.MALE) {
            bmr = term1.add(term2).subtract(term3).add(new BigDecimal("5"));
        } else {
            bmr = term1.add(term2).subtract(term3).subtract(new BigDecimal("161"));
        }
        BigDecimal dailyKcal = bmr.multiply(activityFactor).setScale(2, RoundingMode.HALF_UP);

        BigDecimal dailyProtein = new BigDecimal("0.8").multiply(weight).setScale(2, RoundingMode.HALF_UP);
        BigDecimal fatCalories = dailyKcal.multiply(new BigDecimal("0.25"));
        BigDecimal dailyFat = fatCalories.divide(new BigDecimal("9"), 2, RoundingMode.HALF_UP);

        BigDecimal heightInMeters = height.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal bmi = weight.divide(heightInMeters.multiply(heightInMeters), 2, RoundingMode.HALF_UP);

        return HealthMetric.builder()
                .user(user)
                .bmi(bmi)
                .dailyKcal(dailyKcal)
                .dailyProtein(dailyProtein)
                .dailyFat(dailyFat)
                .build();
    }


}

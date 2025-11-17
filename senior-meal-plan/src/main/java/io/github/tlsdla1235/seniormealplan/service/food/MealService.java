package io.github.tlsdla1235.seniormealplan.service.food;

import io.github.tlsdla1235.seniormealplan.domain.Meal;
import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.dto.meal.*;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.MealForWeeklyDto;
import io.github.tlsdla1235.seniormealplan.repository.MealRepository;
import io.github.tlsdla1235.seniormealplan.service.admin.S3UploadService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealService {
    private final MealRepository mealRepository;
    private final S3UploadService s3UploadService;


    @Transactional(readOnly = true)
    public List<MealCachedDto> getTodayMeals(User user) {
        LocalDate today = LocalDate.now();
        var cached = mealRepository.findByUserAndMealDateWithFoods(user, today)
                .stream().map(MealCachedDto::from).toList();
        log.info("Cache Miss 사용자 id:{} todayMeals size={}", user.getUserId(), cached.size());
        return cached;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "mealsByDate", key = "#user.userId + '_' + #date",
            unless = "#result == null || #result.isEmpty()")
    public List<MealCachedDto> getMealsByDate(User user, LocalDate date) {
        var cached = mealRepository.findByUserAndMealDateWithFoods(user, date)
                .stream().map(MealCachedDto::from).toList();
        log.info("Cache Miss 사용자 id:{} mealsByDate({}) size={}", user.getUserId(), date, cached.size());
        return cached;
    }

    public List<Meal> findByUserAndMealDateWithFoods(User user, LocalDate date) {
        return mealRepository.findByUserAndMealDateWithFoods(user, date);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "todayMeals",
                    key = "#result.user.userId",
                    condition = "#result.mealDate.isEqual(T(java.time.LocalDate).now())"),
            @CacheEvict(value = "mealsByDate",
                    key = "#result.user.userId + '_' + #result.mealDate.toString()")
    })
    public Meal updateMealWithAnalysis(AnalysisMealResultDto dto) {
        Meal meal = mealRepository.findById(dto.mealId())
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 식사 데이터를 찾을 수 없습니다: " + dto.mealId()));

        meal.setTotalKcal(dto.totalKcal());
        meal.setTotalCalcium(dto.totalCalcium());
        meal.setTotalCarbs(dto.totalCarbs());
        meal.setTotalProtein(dto.totalProtein());
        meal.setTotalFat(dto.totalFat());

        meal.setDairyIntake(dto.isDairyIntake());
        meal.setVitaminCIntake(dto.isVitaminCIntake());
        meal.setVitaminBIntake(dto.isVitaminBIntake());
        meal.setFishIntake(dto.isFishIntake());
        meal.setNutsIntake(dto.isNutsIntake());
        meal.setVegetableOilIntake(dto.isVegetableOilIntake());
        meal.setUnrefinedCarbsIntake(dto.isUnrefinedCarbsIntake());

        log.info("Meal(ID: {}) updated with analysis.", meal.getMealId());
        return meal; // ★ 중요
    }

    public List<LocalDate> getAllMealDateFromUser(User user) {
        List<LocalDate> mealDates = mealRepository.findDistinctMealDatesByUser(user);
        log.info("userid:{}에 대한 getAllMealDateFromUser 반환 결과 : {}", user.getUserId(), mealDates);
        return mealDates;
    }

    public List<MealForWeeklyDto> getMealsForLastWeek(User user, LocalDate date) {
        LocalDate lastMonday = date.minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate lastSunday = date.minusWeeks(1).with(DayOfWeek.SUNDAY);

        // 2. Repository를 통해 해당 기간의 식사 기록을 조회
        List<Meal> meals = mealRepository.findByUserAndMealDateBetweenWithFoods(user, lastMonday, lastSunday);

        // 3. DTO 리스트로 변환하여 반환 (MealForWeeklyDto::fromMeal 사용)
        List<MealForWeeklyDto> mealDtos = meals.stream()
                .map(MealForWeeklyDto::fromMeal) // MealResponseDto::from -> MealForWeeklyDto::fromMeal
                .collect(Collectors.toList());

        log.info("사용자 id:{}에 대한 지난주 식사(getMealsForLastWeek) 결과값: {}", user.getUserId(), mealDtos);
        return mealDtos;
    }

    public List<MealForWeeklyDto> getMealsForCurrentWeek(User user, LocalDate date) {
        // 1. 지난주의 월요일과 일요일 날짜 계산
        LocalDate lastMonday = date.with(DayOfWeek.MONDAY);
        LocalDate lastSunday = date.with(DayOfWeek.SUNDAY);


        // 2. Repository를 통해 해당 기간의 식사 기록을 조회
        // (MealRepository에 해당 메서드가 있다고 가정)
        List<Meal> meals = mealRepository.findByUserAndMealDateBetweenWithFoods(user, lastMonday, lastSunday);

        // 3. DTO 리스트로 변환하여 반환 (MealForWeeklyDto::fromMeal 사용)
        List<MealForWeeklyDto> mealDtos = meals.stream()
                .map(MealForWeeklyDto::fromMeal) // MealResponseDto::from -> MealForWeeklyDto::fromMeal
                .collect(Collectors.toList());

        log.info("사용자 id:{}에 대한 지난주 식사(getMealsForLastWeek) 결과값: {}", user.getUserId(), mealDtos);
        return mealDtos;
    }

    public List<MealImageWithFoodNameDto> findByUserAndMealDateIn(User user, Collection<LocalDate> dates)
    {
        List<Meal> meals = mealRepository.findByUserAndMealDateIn(user, dates);
        return meals.stream().map(meal->{
            String presignedUrl = s3UploadService.generatePresignedUrlForGet(meal.getUniqueFileName());
            return MealImageWithFoodNameDto.fromMeal(meal, presignedUrl);
        }).toList();

    }

    @Transactional(readOnly = true) // 데이터 조작이 없는 순수 조회이므로 readOnly=true 권장
    public List<User> findUsersWithMealsOnDate(LocalDate date) {
        log.info("{} 날짜에 식사 기록이 있는 유저 조회를 시작합니다.", date);
        List<User> users = mealRepository.findDistinctUsersByMealDate(date);
        log.info("{} 날짜에 총 {}명의 유저가 조회되었습니다.", date, users.size());
        return users;
    }


    @Transactional(readOnly = true)
    public List<User> findUsersWithMealsBetweenDates(LocalDate startDate, LocalDate endDate) {
        log.info("{}부터 {} 사이 식사 기록이 있는 유저 조회를 시작합니다.", startDate, endDate);
        List<User> users = mealRepository.findDistinctUsersByMealDateBetween(startDate, endDate);
        log.info("해당 기간 총 {}명의 유저가 조회되었습니다.", users.size());
        return users;
    }
}

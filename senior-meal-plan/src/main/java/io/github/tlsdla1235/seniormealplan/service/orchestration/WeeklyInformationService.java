package io.github.tlsdla1235.seniormealplan.service.orchestration;

import io.github.tlsdla1235.seniormealplan.domain.Meal;
import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.report.DailyReport;
import io.github.tlsdla1235.seniormealplan.dto.dailyreport.DailyReportForWeeklyDto;
import io.github.tlsdla1235.seniormealplan.dto.dailyreport.dtoHell;
import io.github.tlsdla1235.seniormealplan.dto.meal.MealImageDto;
import io.github.tlsdla1235.seniormealplan.dto.meal.MealImageWithFoodNameDto;
import io.github.tlsdla1235.seniormealplan.repository.DailyReportRepository;
import io.github.tlsdla1235.seniormealplan.repository.MealRepository;
import io.github.tlsdla1235.seniormealplan.service.food.MealService;
import io.github.tlsdla1235.seniormealplan.service.report.DailyReportService;
import io.github.tlsdla1235.seniormealplan.service.report.WeeklyReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeeklyInformationService {
    private final DailyReportService dailyReportService;
    private final MealService mealService;
    private final MealRepository mealRepository;
    private final WeeklyReportService weeklyReportService;


    public List<dtoHell> ResponseBetweenDates(User user, LocalDate startDate, LocalDate endDate)
    {
        List<DailyReportForWeeklyDto> dailyReports = dailyReportService.getDailyReportBetweenDate(user, startDate, endDate);
        if (dailyReports.isEmpty()) {
            return Collections.emptyList();
        }
        List<LocalDate> reportedDates = dailyReports.stream()
                .map(DailyReportForWeeklyDto::date)
                .toList();
        List<MealImageWithFoodNameDto> meals = mealService.findByUserAndMealDateIn(user, reportedDates);
        log.info("ResponseBetweenDates에서 meals 결과값 {}", meals);
        Map<LocalDate, List<MealImageWithFoodNameDto>> mealsByDateMap = meals.stream()
                .collect(Collectors.groupingBy(MealImageWithFoodNameDto::date));

        return dailyReports.stream()
                .map(report -> {
                    List<MealImageWithFoodNameDto> mealsForDay = mealsByDateMap.getOrDefault(report.date(), Collections.emptyList());
                    return new dtoHell(
                            report.ReportId(),
                            report.date(),
                            report.summarizeScore(),
                            report.basicScore(),
                            report.macularDegenerationScore(),
                            report.hypertensionScore(),
                            report.myocardialInfarctionScore(),
                            report.sarcopeniaScore(),
                            report.hyperlipidemiaScore(),
                            report.boneDiseaseScore(),
                            mealsForDay
                    );
                })
                .toList();
    }
}

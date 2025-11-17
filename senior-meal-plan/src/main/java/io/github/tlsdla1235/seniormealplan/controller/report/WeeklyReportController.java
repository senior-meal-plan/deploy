package io.github.tlsdla1235.seniormealplan.controller.report;

import io.github.tlsdla1235.seniormealplan.config.JwtAuthFilter;
import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.dto.async.WeeklyReportGenerationData;
import io.github.tlsdla1235.seniormealplan.dto.dailyreport.dtoHell;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.GetWeeklyReportDto;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.SimpleWeeklyReportDto;
import io.github.tlsdla1235.seniormealplan.service.orchestration.GenerateWeeklyReportsService;
import io.github.tlsdla1235.seniormealplan.service.orchestration.WeeklyInformationService;
import io.github.tlsdla1235.seniormealplan.service.report.WeeklyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/report/weekly")
public class WeeklyReportController {
    private final WeeklyReportService weeklyReportService;
    private final WeeklyInformationService weeklyInformationService;
    private final GenerateWeeklyReportsService generateWeeklyReportsService;

    @GetMapping("/dates")
    public ResponseEntity<List<SimpleWeeklyReportDto>> getAllWRDateFromUser(@AuthenticationPrincipal JwtAuthFilter.JwtPrincipal me)
    {
        List<SimpleWeeklyReportDto> reportDates = weeklyReportService.getAllWRDateFromUser(
                User.builder().userId(me.userId()).build()
        );
        return ResponseEntity.ok(reportDates);
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<GetWeeklyReportDto> getWeeklyReport(@PathVariable Long reportId) {
        GetWeeklyReportDto reportDto = weeklyReportService.getReport(reportId);
        return ResponseEntity.ok(reportDto);
    }

    @GetMapping("/daily-summary")
    public ResponseEntity<List<dtoHell>> getDailyReportsWithMeals(
            @AuthenticationPrincipal JwtAuthFilter.JwtPrincipal me,
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate)
    {
        User user = User.builder().userId(me.userId()).build();
        List<dtoHell> dailySummaries = weeklyInformationService.ResponseBetweenDates(
                user,
                startDate,
                endDate
        );
        return ResponseEntity.ok(dailySummaries);
    }

    @GetMapping("/test/createWeeklyReport")
    public ResponseEntity<WeeklyReportGenerationData> testCreate(@AuthenticationPrincipal JwtAuthFilter.JwtPrincipal me,
                                                                 @RequestParam("Date") LocalDate Date)
    {
        WeeklyReportGenerationData res = generateWeeklyReportsService.generateWeeklyReportsForTest(User.builder().userId(me.userId()).build(), Date);
        return ResponseEntity.ok(res);
    }

}

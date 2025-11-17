package io.github.tlsdla1235.seniormealplan.service.report;

import io.github.tlsdla1235.seniormealplan.domain.Meal;
import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.ReportStatus;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.Severity;
import io.github.tlsdla1235.seniormealplan.domain.report.DailyReport;
import io.github.tlsdla1235.seniormealplan.dto.async.DailyReportGenerationData;
import io.github.tlsdla1235.seniormealplan.dto.dailyreport.DailyReportAnalysisResultDto;
import io.github.tlsdla1235.seniormealplan.dto.dailyreport.DailyReportForWeeklyDto;
import io.github.tlsdla1235.seniormealplan.dto.dailyreport.DailyReportResponseDto;
import io.github.tlsdla1235.seniormealplan.dto.meal.AnalysisMealRequestDto;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.DailyReportsForWeeklyReportDto;
import io.github.tlsdla1235.seniormealplan.repository.DailyReportRepository;
import io.github.tlsdla1235.seniormealplan.service.admin.S3UploadService;
import io.github.tlsdla1235.seniormealplan.service.food.MealService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyReportService {
    private final DailyReportRepository dailyReportRepository;
    private final MealService mealService;

    public DailyReport createPendingDailyReport(User user, LocalDate reportDate) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("유저가 유효하지 않음");
        }
        if (reportDate == null) {
            throw new IllegalArgumentException("날짜입력이 유효하지 않음");
        }
        DailyReport dailyReport = new DailyReport(user, reportDate);
        DailyReport savedReport = dailyReportRepository.save(dailyReport);
        log.info("데일리 레포트가 pending 상태로  생성되었습니다. ID: {} 유저ID: {} 날짜 {}.",
                savedReport.getReportId(), user.getUserId(), reportDate);
        return savedReport;
    }

    public DailyReport getDailyReport(Long dailyReportId) {
        return dailyReportRepository.findById(dailyReportId)
                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 데일리 리포트를 찾을 수 없습니다: " + dailyReportId));
    }

    public void updateReportWithAnalysis(DailyReportAnalysisResultDto result) {
        DailyReport report = this.getDailyReport(result.reportId());
        if (!"SUCCESS".equals(result.status())) {
            log.error("데일리 리포트(ID: {}) 분석 실패. 원인: {}", result.reportId(), result.errorMessage());
            report.markAsFailed();
            return;
        }

        try {
            // String으로 받은 severity 값을 Enum으로 변환합니다.
            Severity severityEnum = Severity.valueOf(result.severity().toUpperCase());

            // 엔티티 내부의 업데이트 메서드를 호출하여 모든 필드를 갱신하고 상태를 COMPLETE로 변경합니다.
            report.updateWithAnalysis(
                    result.totalKcal(),
                    result.totalProtein(),
                    result.totalCarbs(),
                    result.totalFat(),
                    result.totalCalcium(),
                    result.summary(),
                    severityEnum,
                    result.summarizeScore(),
                    result.basicScore(),
                    result.macularDegenerationScore(),
                    result.hyperlipidemiaScore(),
                    result.myocardialInfarctionScore(),
                    result.sarcopeniaScore(),
                    result.boneDiseaseScore(),
                    result.hypertensionScore()
            );
            log.info("데일리 리포트(ID: {})가 분석 결과로 업데이트되었습니다. 상태: COMPLETE", report.getReportId());

        } catch (IllegalArgumentException e) {
            log.error("AI 서버로부터 유효하지 않은 Severity 값을 받았습니다: '{}'. 리포트(ID: {})를 FAILED로 처리합니다.",
                    result.severity(), report.getReportId());
            report.markAsFailed();
        } catch (Exception e) {
            log.error("데일리 리포트(ID: {}) 업데이트 중 알 수 없는 오류 발생", report.getReportId(), e);
            report.markAsFailed();
        }
    }

    public DailyReportResponseDto getDailyReportByDate(User user ,LocalDate date) {
        DailyReport dailyReport = dailyReportRepository.findByUserAndReportDate(user, date).orElseThrow(EntityNotFoundException::new);
        if (dailyReport.getStatus() != ReportStatus.COMPLETE) {
            throw new IllegalStateException("아직 처리 중이거나 실패한 리포트입니다. status: " + dailyReport.getStatus());
        }
        log.info("user id: {}에 대한 {}의 daily report 조회가 완료되었습니다.", user.getUserId(), date);
        DailyReportResponseDto responseDto = DailyReportResponseDto.fromDailyReport(dailyReport);
        log.info("{}", responseDto);
        return responseDto;
    }

    public List<DailyReportsForWeeklyReportDto> getCompletedReportsForLastWeek(User user, LocalDate date) {

        LocalDate lastMonday = date.minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate lastSunday = date.minusWeeks(1).with(DayOfWeek.SUNDAY);

        List<DailyReport> reports = dailyReportRepository
                .findByUserAndReportDateBetween(user, lastMonday, lastSunday);

        return reports.stream()
                .map(DailyReportsForWeeklyReportDto::from) // DTO 변환
                .filter(Objects::nonNull)                  // null이 아닌 것만 필터링
                .collect(Collectors.toList());             // 리스트로 수집
    }

    public List<DailyReportForWeeklyDto> getDailyReportBetweenDate(User user, LocalDate startDate, LocalDate endDate) {
        List<DailyReport> list =dailyReportRepository.findByUserAndReportDateBetween(user, startDate, endDate);
        return list.stream().map(DailyReportForWeeklyDto::from).collect(Collectors.toList());
    }


    @Transactional
    public List<DailyReportGenerationData> createPendingReportsInTransaction(List<User> users, LocalDate date) {
        List<DailyReportGenerationData> generatedDataList = new ArrayList<>();

        for (User user : users) {
            List<Meal> meals = mealService.findByUserAndMealDateWithFoods(user, date);

            if (meals.isEmpty()) {
                log.warn("유저 ID: {}는 {}에 식사 기록이 없어 리포트를 생성하지 않습니다.", user.getUserId(), date);
                continue;
            }

            DailyReport report = createPendingDailyReport(user, date);
            user.setLastDailyReportDate(date);

            log.info("유저 ID: {}의 {} 날짜 리포트 생성 완료. Report ID: {}", user.getUserId(), date, report.getReportId());
            generatedDataList.add(new DailyReportGenerationData(user, meals, report));
        }

        return generatedDataList;
    }




    /**
     * 테스트용 함수 - 원래 로직은 지난주를 검사해야하지만, 테스트를 위해 이번주
     * @param user
     * @param date
     * @return
     */
    public List<DailyReportsForWeeklyReportDto> getCompletedReportsForCurrentWeek(User user, LocalDate date) {
        // 2. '지난주'의 월요일과 일요일 날짜 계산

        // 지난주 월요일: (오늘 날짜에서 1주일을 뺀 날짜)가 포함된 주의 월요일
        LocalDate lastMonday = date.with(DayOfWeek.MONDAY);
        // 지난주 일요일: 계산된 월요일에서 6일을 더함
        LocalDate lastSunday = date.with(DayOfWeek.SUNDAY);

        // 3. Repository를 통해 해당 기간의 DailyReport 엔티티 조회
        List<DailyReport> reports = dailyReportRepository
                .findByUserAndReportDateBetween(user, lastMonday, lastSunday);

        // 4. Stream을 사용하여 DTO 리스트로 변환
        return reports.stream()
                .map(DailyReportsForWeeklyReportDto::from) // DTO 변환
                .filter(Objects::nonNull)                  // null이 아닌 것만 필터링
                .collect(Collectors.toList());             // 리스트로 수집
    }
}

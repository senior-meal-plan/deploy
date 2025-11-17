package io.github.tlsdla1235.seniormealplan.service.report;

import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.Severity;
import io.github.tlsdla1235.seniormealplan.domain.report.WeeklyReport;
import io.github.tlsdla1235.seniormealplan.dto.async.WeeklyReportGenerationData;
import io.github.tlsdla1235.seniormealplan.dto.user.WhoAmIDto;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.AnalysisResultDto.AnalysisWeeklyReportDto;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.AnalysisResultDto.WeeklyAnalysisResultDto;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.DailyReportsForWeeklyReportDto;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.GetWeeklyReportDto;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.MealForWeeklyDto;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.SimpleWeeklyReportDto;
import io.github.tlsdla1235.seniormealplan.repository.WeeklyReportRepository;
import io.github.tlsdla1235.seniormealplan.service.food.MealService;
import io.github.tlsdla1235.seniormealplan.service.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeeklyReportService {
    private final WeeklyReportRepository weeklyReportRepository;
    private final UserService userService;
    private final DailyReportService dailyReportService;
    private final MealService mealService;


    public WeeklyReport createPendingWeeklyReport(User user, LocalDate dateForWeek) {
        if (user == null || user.getUserId() == null) {
            throw new IllegalArgumentException("User is not valid.");
        }
        if (dateForWeek == null) {
            throw new IllegalArgumentException("Date is not valid.");
        }

        LocalDate weekStart = dateForWeek.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = dateForWeek.with(DayOfWeek.SUNDAY);

        WeeklyReport weeklyReport = new WeeklyReport(user, weekStart, weekEnd);

        WeeklyReport savedReport = weeklyReportRepository.save(weeklyReport);

        log.info("Weekly Report created in PENDING state. ID: {}, UserID: {}, Week: {} ~ {}",
                savedReport.getReportId(), user.getUserId(), weekStart, weekEnd);
        return savedReport;
    }

    public void updatePendingWeeklyReport(WeeklyAnalysisResultDto resultDto) {
        AnalysisWeeklyReportDto updateContent =  resultDto.weeklyReport();
        log.info("report Id:{}에 대한 주간 리포트 업데이트를 진행합니다.", updateContent.WeeklyReportId());
        WeeklyReport weeklyReport = weeklyReportRepository.findById(updateContent.WeeklyReportId()).orElseThrow();
        Severity severity = Severity.valueOf(updateContent.severity().toUpperCase());
        weeklyReport.UpdateWithAnalysis(updateContent.summaryGoodPoint(), updateContent.summaryBadPoint(),
                                        updateContent.summaryAiRecommend(), severity);
        log.info("report id:{}에 대한 주간 리포트 업데이트가 완료 되었습니다.",weeklyReport.getReportId());
    }


    @Transactional
    public List<WeeklyReportGenerationData> createPendingWeeklyReportsInTransaction(
            List<User> users, LocalDate referenceDate) {

        List<WeeklyReportGenerationData> generatedDataList = new ArrayList<>();
        LocalDate lastWeekDate = referenceDate.minusWeeks(1);
        for (User user : users) {
            log.info("유저 ID: {}의 주간 리포트 생성 시작", user.getUserId());

            // 1. 필요한 DTO 데이터 수집 (기존 로직 동일)
            // (referenceDate = '이번주 월요일' -> get...ForLastWeek가 '지난주'를 계산)
            WhoAmIDto userdto = userService.whoAmI(user);
            List<DailyReportsForWeeklyReportDto> dailyReportDto =
                    dailyReportService.getCompletedReportsForLastWeek(user, referenceDate);
            List<MealForWeeklyDto> mealsDto =
                    mealService.getMealsForLastWeek(user, referenceDate);

            if (dailyReportDto.isEmpty() && mealsDto.isEmpty()) {
                log.info("유저 ID: {}는 지난주 식사/데일리 리포트 기록이 없어 주간 리포트를 생성하지 않습니다.", user.getUserId());
                continue;
            }
            User userForReport = new User();
            userForReport.setUserId(user.getUserId());
            userForReport.setUserGender(user.getUserGender());
            userForReport.setAge(user.getAge());
            userForReport.setUserInputId(user.getUserInputId());
            userForReport.setUserName(user.getUserName());
            userForReport.setUserHeight(user.getUserHeight());
            // 2. 주간 리포트(껍데기) 생성
            WeeklyReport newReport = createPendingWeeklyReport(user, lastWeekDate);

            // 3. 비동기 요청에 필요한 데이터 패키징
            generatedDataList.add(new WeeklyReportGenerationData(
                    userForReport, newReport, userdto, dailyReportDto, mealsDto
            ));
        }

        return generatedDataList;
    }


    @Transactional
    public WeeklyReportGenerationData createWeeklyReportsForTest(
            User user, LocalDate referenceDate) {

        List<WeeklyReportGenerationData> generatedDataList = new ArrayList<>();
        LocalDate lastWeekDate = referenceDate.minusWeeks(1);

        log.info("유저 ID: {}의 주간 리포트 생성 시작", user.getUserId());

        WhoAmIDto userdto = userService.whoAmI(user);
        List<DailyReportsForWeeklyReportDto> dailyReportDto =
                    dailyReportService.getCompletedReportsForLastWeek(user, referenceDate);
        List<MealForWeeklyDto> mealsDto =
                mealService.getMealsForLastWeek(user, referenceDate);

            if (dailyReportDto.isEmpty() && mealsDto.isEmpty()) {
                log.info("유저 ID: {}는 지난주 식사/데일리 리포트 기록이 없어 주간 리포트를 생성하지 않습니다.", user.getUserId());
            }

            WeeklyReport newReport = createPendingWeeklyReport(user, lastWeekDate);

            generatedDataList.add(new WeeklyReportGenerationData(
                    user, newReport, userdto, dailyReportDto, mealsDto
            ));

        return generatedDataList.get(0);
    }


    /**
     * 이 아래로 유저가 사용하는 조회
     *
     */
    public List<SimpleWeeklyReportDto> getAllWRDateFromUser(User user) {
        return weeklyReportRepository.findSimpleReportsByUserOrderByWeekEndDesc(user);
    }

    @PreAuthorize("@securityService.isReportOwner(#reportId)")
    public GetWeeklyReportDto getReport(Long reportId) {
        WeeklyReport persist =  weeklyReportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("리포트를 찾을 수 없습니다. ID: " + reportId));
        return GetWeeklyReportDto.toDto(persist);
    }

}

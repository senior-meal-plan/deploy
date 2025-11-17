package io.github.tlsdla1235.seniormealplan.service.report;

import io.github.tlsdla1235.seniormealplan.domain.Meal;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.ReportStatus;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.Severity;
import io.github.tlsdla1235.seniormealplan.domain.report.MealReport;
import io.github.tlsdla1235.seniormealplan.dto.meal.AnalysisMealResultDto;
import io.github.tlsdla1235.seniormealplan.dto.mealreport.MealReportResponseDto;
import io.github.tlsdla1235.seniormealplan.repository.MealReportRepository;
import io.github.tlsdla1235.seniormealplan.repository.ReportRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MealReportService {
    private final ReportRepository reportRepository;
    private final MealReportRepository mealReportRepository;


    @Transactional
    public MealReport createPendingMealReport(Meal meal) {
        if (meal == null || meal.getMealId() == null) {
            throw new IllegalArgumentException("Meal must be saved and have an ID to create a report.");
        }
        if (meal.getUser() == null) {
            throw new IllegalArgumentException("Meal must have a User to create a report.");
        }

        MealReport mealReport = new MealReport(meal);

        mealReport.setUser(meal.getUser());
        mealReport.setReportDate(meal.getMealDate());

        // save()가 호출될 때 Report 엔티티의 @PrePersist가 status를 PENDING으로 설정합니다.
        MealReport savedReport = mealReportRepository.save(mealReport);
        log.info("Pending MealReport(ID: {}) created for Meal(ID: {})", savedReport.getReportId(), meal.getMealId());
        return savedReport;
    }

    public void updateMealReportWithAnalysis(AnalysisMealResultDto dto) {
        // AI 서버가 mealId를 반환해준다고 가정
        Long mealId = dto.mealId();
        if (mealId == null) {
            throw new IllegalArgumentException("Meal ID is required to update analysis.");
        }

        // mealId를 이용해 MealReport를 찾습니다.
        MealReport mealReport = mealReportRepository.findByMeal_MealId(mealId)
                .orElseThrow(() -> new EntityNotFoundException("MealReport not found for Meal ID: " + mealId));

        // PENDING 상태일 때만 업데이트를 진행합니다.
        if (mealReport.getStatus() != ReportStatus.PENDING) {
            log.warn("MealReport(ID: {}) is not in PENDING state. Current state: {}",
                    mealReport.getReportId(), mealReport.getStatus());
            return;
        }

        try {
            Severity severityEnum = Severity.valueOf(dto.Severity().toUpperCase());

            // 2. 변환된 enum 값을 엔티티 메서드에 전달합니다.
            mealReport.updateWithAnalysis(
                    dto.Summary(),
                    severityEnum  // String 대신 변환된 enum을 전달
            );

            // 3. 상태를 COMPLETE로 변경
            mealReport.changeStatus(ReportStatus.COMPLETE);
            log.info("MealReport(ID: {}) updated with analysis. Status changed to COMPLETE.", mealReport.getReportId());

        } catch (IllegalArgumentException e) {
            // 4. (매우 중요) 만약 AI 서버가 "UNKNOWN"이나 "N/A"처럼
            // Severity enum에 정의되지 않은 문자열을 보내면 valueOf()에서 이 예외가 발생합니다.
            log.error("Invalid Severity value received from AI: '{}'. For MealReport(ID: {})",
                    dto.Severity(), mealReport.getReportId(), e);

            // 이 경우, 리포트 상태를 FAILED로 변경하여 관리자가 확인하도록 합니다.
            mealReport.changeStatus(ReportStatus.FAILED);

        } catch (Exception e) {
            // 5. 그 외 다른 예외 처리
            log.error("Failed to update MealReport(ID: {}) with analysis.", mealReport.getReportId(), e);
            mealReport.changeStatus(ReportStatus.FAILED);
        }
    }

    @PreAuthorize("@securityService.isMealOwner(#meal.mealId)")
    public MealReportResponseDto getMealReportByMealId(Meal meal) {
        MealReport mealReport = mealReportRepository.findByMeal_MealId(meal.getMealId()).orElseThrow(() -> new EntityNotFoundException("MealReport not found for Meal ID: " + meal.getMealId()));
        MealReportResponseDto responseDto = MealReportResponseDto.fromMealReport(mealReport);
        log.info("getMealReportByMealId에서 mealId :{}에 대한 호출, {}", meal.getMealId(), responseDto);
        return responseDto;
    }

}

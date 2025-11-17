package io.github.tlsdla1235.seniormealplan.service;


import io.github.tlsdla1235.seniormealplan.config.JwtAuthFilter;
import io.github.tlsdla1235.seniormealplan.domain.Meal;
import io.github.tlsdla1235.seniormealplan.domain.report.Report;
import io.github.tlsdla1235.seniormealplan.repository.MealRepository;
import io.github.tlsdla1235.seniormealplan.repository.ReportRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("securityService")
@RequiredArgsConstructor
@Slf4j
public class ResourceSecurityService {
    private final MealRepository mealRepository;
    private final ReportRepository reportRepository;

    public boolean isMealOwner(Long mealId) {
        if (mealId == null) return false;

        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new EntityNotFoundException("meal이 발견되지 않았습니다. mealId: " + mealId));

        Long currentUserId = getCurrentUserId();
        return meal.getUser().getUserId().equals(currentUserId);
    }

    public boolean isReportOwner(Long reportId) {
        if (reportId == null) return false;

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("레포트가 발견되지 않았습니다. reportId: " + reportId));

        Long currentUserId = getCurrentUserId();
        return report.getUser().getUserId().equals(currentUserId);
    }


    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("인증되지 않은 유저");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof String && "anonymousUser".equals(principal)) {
            throw new AccessDeniedException("Anonymous user is not allowed");
        }

        if (principal instanceof JwtAuthFilter.JwtPrincipal) {
            return ((JwtAuthFilter.JwtPrincipal) principal).userId();
        }

        log.error("예외 상황 발생: {}", principal.getClass().getName());
        throw new RuntimeException("인증 오류.");
    }
}

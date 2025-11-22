package io.github.tlsdla1235.seniormealplan.controller.temp;

import io.github.tlsdla1235.seniormealplan.service.orchestration.GenerateDailyReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/test/batch")
@RequiredArgsConstructor
@Slf4j
public class BatchTestController {

    private final GenerateDailyReportService generateDailyReportService;

    // [Before] 비효율적인 방식 실행
    @PostMapping("/before")
    public ResponseEntity<String> triggerBefore(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (date == null) date = LocalDate.now().minusDays(1);

        log.info("=== [Test] Before Scenario Triggered ===");
        generateDailyReportService.executeBatch_Before(date);

        return ResponseEntity.ok("Before Batch Completed");
    }

    // [After] 최적화된 방식 실행
    @PostMapping("/after")
    public ResponseEntity<String> triggerAfter(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (date == null) date = LocalDate.now().minusDays(1);

        log.info("=== [Test] After Scenario Triggered ===");
        generateDailyReportService.executeBatch_After(date);

        return ResponseEntity.ok("After Batch Completed");
    }
}
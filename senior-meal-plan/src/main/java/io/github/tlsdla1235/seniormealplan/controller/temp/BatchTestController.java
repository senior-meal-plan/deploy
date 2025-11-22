package io.github.tlsdla1235.seniormealplan.controller.test;

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

    // k6가 호출할 api
    @PostMapping("/daily-report")
    public ResponseEntity<String> triggerDailyReportBatch(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (date == null) {
            date = LocalDate.now().minusDays(1); // 기본값: 어제
        }

        long startTime = System.currentTimeMillis();
        log.info("[Batch Test] 데일리 리포트 생성 배치 시작 (대상 날짜: {})", date);

        // 기존에 만들어두신 테스트용 메서드 호출
        generateDailyReportService.generateDailyReportsBatchTest(date);

        long duration = System.currentTimeMillis() - startTime;
        log.info("[Batch Test] 배치 종료. 소요 시간: {}ms", duration);

        return ResponseEntity.ok("Batch Completed. Duration: " + duration + "ms");
    }
}
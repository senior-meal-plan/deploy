package io.github.tlsdla1235.seniormealplan.controller.aiServerCon;

import com.amazonaws.Response;
import io.github.tlsdla1235.seniormealplan.config.JwtAuthFilter;
import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.dto.async.WeeklyReportGenerationData;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.AnalysisResultDto.WeeklyAnalysisResultDto;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.WeeklyReportGenerateRequestDto;
import io.github.tlsdla1235.seniormealplan.service.orchestration.GenerateWeeklyReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/webhooks/weekly")
public class WeeklyWebhookController {
    private final GenerateWeeklyReportsService generateWeeklyReportsService;

    @PostMapping("/analysis-complete")
    public ResponseEntity<String> complete(@RequestBody WeeklyAnalysisResultDto resultDto)
    {
        generateWeeklyReportsService.updateWeeklyResult(resultDto);
        return ResponseEntity.ok("Success");
    }


    @PostMapping("/dtoCheckApiNot")
    public ResponseEntity<String> check(@RequestBody WeeklyReportGenerateRequestDto requestDto)
    {
        return ResponseEntity.ok("Success");
    }

    @PostMapping("/dtoCheckApi2")
    public ResponseEntity<String> check(@RequestBody List<WeeklyReportGenerationData> requestDto)
    {
        return ResponseEntity.ok("Success");
    }

    @PostMapping("/testCode")
    public ResponseEntity<String> test(@AuthenticationPrincipal JwtAuthFilter.JwtPrincipal me)
    {
        generateWeeklyReportsService.createRequestDto(User.builder().userId(me.userId()).build());
        return ResponseEntity.ok("Success");
    }

    @GetMapping("/test")
    public ResponseEntity<List<WeeklyReportGenerationData>> testt()
    {
        List<WeeklyReportGenerationData> temp = generateWeeklyReportsService.generateWeeklyReportsBatchTest();
        return ResponseEntity.ok(temp);
    }


}

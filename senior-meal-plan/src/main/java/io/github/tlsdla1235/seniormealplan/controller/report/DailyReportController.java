package io.github.tlsdla1235.seniormealplan.controller.report;

import io.github.tlsdla1235.seniormealplan.config.JwtAuthFilter;
import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.dto.dailyreport.DailyReportResponseDto;
import io.github.tlsdla1235.seniormealplan.service.orchestration.GenerateDailyReportService;
import io.github.tlsdla1235.seniormealplan.service.report.DailyReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/report/daily")
public class DailyReportController {
    private final DailyReportService dailyReportService;
    private final GenerateDailyReportService generateDailyReportService;

    @GetMapping("/today")
    public ResponseEntity<DailyReportResponseDto> testFunction(@AuthenticationPrincipal JwtAuthFilter.JwtPrincipal me)
    {
        LocalDate today = LocalDate.now();
        DailyReportResponseDto dto =  dailyReportService.getDailyReportByDate(User.builder().userId(me.userId()).build(), today);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @GetMapping
    public ResponseEntity<DailyReportResponseDto> getDailyReportByDate(
            @AuthenticationPrincipal JwtAuthFilter.JwtPrincipal me,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("일일 리포트 조회 요청 - User ID: {}, Date: {}", me.userId(), date);
        User user = User.builder().userId(me.userId()).build();

        DailyReportResponseDto dto = dailyReportService.getDailyReportByDate(user, date);

        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }



    @PostMapping("/temp/me/daily")
    public ResponseEntity<String> generateMyDailyReport(
            @AuthenticationPrincipal JwtAuthFilter.JwtPrincipal me,
            @RequestBody(required = false) LocalDate date) {

        // 1. 인증 정보 확인 (토큰이 없거나 유효하지 않으면 me는 null)
        if (me == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
        }
        User u = new User();
        u.setUserId(me.userId());
        generateDailyReportService.generateReportAndRequestAnalysis(u, date);

        return ResponseEntity.accepted().body(
                "데일리 리포트 생성 요청이 접수되었습니다. User ID: " + u.getUserId() + ", Date: " + date
        );
    }


}

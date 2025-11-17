package io.github.tlsdla1235.seniormealplan.dto.dailyreport;

import io.github.tlsdla1235.seniormealplan.domain.Meal;
import io.github.tlsdla1235.seniormealplan.dto.meal.AnalyzedFoodDto;
import io.github.tlsdla1235.seniormealplan.dto.meal.MealDto;
import io.github.tlsdla1235.seniormealplan.dto.user.WhoAmIDto;

import java.util.List;

public record DailyReportAnalysisRequestDto(
        Long reportId,          // 생성된 DailyReport의 ID
        WhoAmIDto whoAmIDto,         // 사용자 ID
        List<MealDto> meals,       // Food가 포함된 해당 날짜의 식사 목록
        String callbackUrl      // 분석 결과를 받을 우리 서버의 웹훅 주소
) {
}

package io.github.tlsdla1235.seniormealplan.dto.meal;

import io.github.tlsdla1235.seniormealplan.dto.user.WhoAmIDto;

public record AnalysisMealRequestDto (
        Long mealId,         // 어떤 식단에 대한 분석인지 식별자
        String photoUrl,     // 분석할 사진의 URL
        String callbackUrl,   // 분석 완료 후 결과를 보내줄 우리 서버의 웹훅 주소
        WhoAmIDto whoAmIDto
){
}

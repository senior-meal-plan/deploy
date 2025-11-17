package io.github.tlsdla1235.seniormealplan.exception.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
// NON_NULL: null이 아닌 필드만 JSON에 포함시킵니다. (예: details가 없을 경우)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;       // HTTP 상태 코드 (예: 404)
    private final String error;     // HTTP 상태 메시지 (예: Not Found)
    private final String message;   // 커스텀 예외 메시지
    private final String path;      // 요청 경로
    private final Map<String, String> details; // (선택) 유효성 검사 실패 시 상세 내역
}
package io.github.tlsdla1235.seniormealplan.dto.s3dto;

public record PresignedUrlResponse(
        String presignedUrl,   // 클라이언트가 파일 업로드에 사용할 임시 URL
        String uniqueFileName    // 업로드 완료 후 서버에 알려줄 고유 파일 식별자
) {
}

package io.github.tlsdla1235.seniormealplan.controller.admin;

import io.github.tlsdla1235.seniormealplan.dto.recipe.RecipeGenerateDto;
import io.github.tlsdla1235.seniormealplan.dto.tempdto.HealthTopicCreateRequestDto;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.WeeklyReportGenerateRequestDto;
import io.github.tlsdla1235.seniormealplan.service.admin.AdminService;
import io.github.tlsdla1235.seniormealplan.service.admin.S3UploadService;
import io.github.tlsdla1235.seniormealplan.service.recipe.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/admin") // 관리자용 API 경로 예시
@RequiredArgsConstructor
public class HealthTopicController {
    private final AdminService adminService;
    private final S3UploadService s3UploadService;
    private final RecipeService recipeService;

    @PostMapping("/health-topics")
    public ResponseEntity<Void> createHealthTopic(@RequestBody HealthTopicCreateRequestDto requestDto) {
        adminService.createHealthTopic(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/recipe")
    public ResponseEntity<Void> createRecipe(@RequestBody RecipeGenerateDto recipe) {
        recipeService.save(recipe);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/s3-test/upload")
    public ResponseEntity<String> s3UploadTest() {
        // 매번 다른 이름으로 파일이 생성되도록 현재 시간을 파일명에 포함시킵니다.
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = "test-file_" + now + ".txt";
        String fileContent = "This is a test file uploaded from Spring Boot at " + now;

        // S3 서비스 호출하여 파일 업로드
        String fileUrl = s3UploadService.uploadTestFile(fileName, fileContent);

        String responseMessage = "Test Success! File uploaded to S3. URL: " + fileUrl;
        return ResponseEntity.ok(responseMessage);
    }

    @GetMapping("/tempmap")
    public ResponseEntity<String> tempmap(@RequestBody WeeklyReportGenerateRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}

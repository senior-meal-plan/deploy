package io.github.tlsdla1235.seniormealplan.service.admin;


import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import io.github.tlsdla1235.seniormealplan.dto.s3dto.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploadService {
    private final AmazonS3Client amazonS3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * S3에 테스트용 텍스트 파일을 업로드합니다.
     * @param fileName S3에 저장될 파일 이름
     * @param content 파일에 들어갈 내용
     * @return 업로드된 파일의 URL
     */
    public String uploadTestFile(String fileName, String content) {
        try {
            // 업로드할 파일의 내용을 InputStream으로 변환합니다.
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            InputStream inputStream = new ByteArrayInputStream(contentBytes);

            // S3에 업로드하기 위한 메타데이터를 설정합니다. (여기서는 간단히 생략)
            // ObjectMetadata metadata = new ObjectMetadata();
            // metadata.setContentLength(contentBytes.length);

            // S3에 파일을 업로드합니다.
            log.info("Uploading file to S3 bucket: {}", bucket);
            amazonS3Client.putObject(bucket, fileName, inputStream, null);
            log.info("File upload complete: {}", fileName);

            // 업로드된 파일의 URL을 반환합니다.
            return amazonS3Client.getUrl(bucket, fileName).toString();

        } catch (Exception e) {
            log.error("File upload failed", e);
            throw new RuntimeException("S3 file upload failed", e);
        }
    }

    public String generatePresignedUrlForGet(String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            return null;
        }

        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 5; // 5분
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest presignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, objectKey)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        URL url = amazonS3Client.generatePresignedUrl(presignedUrlRequest);
        return url.toString();
    }

    /**
     * 클라이언트가 S3에 직접 파일을 업로드할 수 있는 Pre-signed URL을 생성합니다.
     * @param originalFileName 클라이언트가 업로드하려는 원본 파일 이름
     * @return Pre-signed URL과 S3에 저장될 고유 파일 이름이 담긴 DTO
     */
    public PresignedUrlResponse generatePresignedUrl(String originalFileName) {
        String uniqueFileName = createUniqueFileName(originalFileName);
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 5; // 5분
        expiration.setTime(expTimeMillis);
        GeneratePresignedUrlRequest presignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, uniqueFileName)
                        .withMethod(HttpMethod.PUT) // PUT 메서드로 업로드하도록 지정
                        .withExpiration(expiration);
        URL url = amazonS3Client.generatePresignedUrl(presignedUrlRequest);
        log.info("Pre-signed URL for {} generated: {}", uniqueFileName, url);

        return new PresignedUrlResponse(url.toString(), uniqueFileName);
    }

    /**
     * S3에 저장된 파일의 영구적인 URL을 조회합니다.
     * @param uniqueFileName S3에 저장된 고유 파일 이름
     * @return DB에 저장하거나 클라이언트에게 보여줄 최종 URL
     */
    public String getFileUrl(String uniqueFileName) {
        return amazonS3Client.getUrl(bucket, uniqueFileName).toString();
    }

    /**
     * 파일 이름의 중복을 피하기 위해 UUID를 사용하여 고유한 파일 이름을 생성합니다.
     * @param originalFileName 원본 파일 이름
     * @return "UUID_원본파일이름" 형식의 새로운 파일 이름
     */
    private String createUniqueFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "_" + originalFileName;
    }
}

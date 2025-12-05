package io.github.tlsdla1235.seniormealplan.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Configuration
public class FcmConfig {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // 1. 이미 초기화된 FirebaseApp이 있는지 확인 (중복 초기화 방지)
        List<FirebaseApp> apps = FirebaseApp.getApps();
        if (apps != null && !apps.isEmpty()) {
            return apps.get(0);
        }

        // 2. resources 폴더에서 파일 읽기
        ClassPathResource resource = new ClassPathResource("firebase-adminsdk.json");
        InputStream inputStream = resource.getInputStream();

        // 3. FirebaseOptions 생성
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(inputStream))
                .build();

        // 4. 초기화
        return FirebaseApp.initializeApp(options);
    }
}
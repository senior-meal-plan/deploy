package io.github.tlsdla1235.seniormealplan.controller.temp;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;

@RestController
public class KakaoTestController {

    @GetMapping("/test/kakao-login")
    public void testLogin(HttpServletResponse response) throws IOException {
        // 1. 본인의 REST API 키 (f7f6d397047de5d669dfe8efc5526183)
        String clientId = "f7f6d397047de5d669dfe8efc5526183";

        // 2. 리다이렉트 URI (설정파일과 똑같아야 함)
        String redirectUri = "https://senior-meal-plan.cloud/login/oauth2/code/kakao";

        // 3. 카카오 로그인 URL 강제 조립
        String kakaoUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code";

        // 4. 강제 이동
        response.sendRedirect(kakaoUrl);
    }
}
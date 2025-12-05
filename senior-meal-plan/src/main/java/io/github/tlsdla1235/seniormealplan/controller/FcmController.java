package io.github.tlsdla1235.seniormealplan.controller;

import io.github.tlsdla1235.seniormealplan.config.JwtAuthFilter;
import io.github.tlsdla1235.seniormealplan.service.fcm.FcmService;
import io.github.tlsdla1235.seniormealplan.service.user.UserService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class FcmController {
    private final UserService userService;

    @PostMapping("/fcm-token")
    public ResponseEntity<String> updateFcmToken(
            Authentication authentication, // ✅ 기존 스타일 유지 (Authentication 객체 받기)
            @RequestBody FcmTokenRequestDto requestDto) {

        JwtAuthFilter.JwtPrincipal jwtPrincipal = (JwtAuthFilter.JwtPrincipal) authentication.getPrincipal();
        Long userId = jwtPrincipal.userId();

        userService.refreshFcmToken(userId, requestDto.getToken());

        return ResponseEntity.ok("토큰 저장 완료");
    }

    @Getter
    @NoArgsConstructor
    public static class FcmTokenRequestDto {
        private String token;
    }


    private final FcmService fcmService;

    @PostMapping("/send")
    public ResponseEntity<String> sendTestMessage(@RequestBody FcmTestRequestDto request) {

        fcmService.sendMessage(
                request.getToken(),
                request.getTitle(),
                request.getBody()
        );

        return ResponseEntity.ok("FCM 전송 요청 완료! 로그를 확인하세요.");
    }

    @Getter
    @NoArgsConstructor
    public static class FcmTestRequestDto {
        private String token; // 아까 발급받은 PC 토큰
        private String title;
        private String body;
    }
}

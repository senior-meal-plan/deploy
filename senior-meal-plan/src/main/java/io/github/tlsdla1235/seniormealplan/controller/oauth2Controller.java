package io.github.tlsdla1235.seniormealplan.controller;

import io.github.tlsdla1235.seniormealplan.config.JwtAuthFilter;
import io.github.tlsdla1235.seniormealplan.dto.oauth.OAuth2RegisterRequest;
import io.github.tlsdla1235.seniormealplan.service.user.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@Slf4j
public class oauth2Controller {
    private final AuthService auth;


    @PostMapping("/oauth2/register")
    public ResponseEntity<Void> registerOAuth2(
            Authentication authentication, // ✅ 자바 기본 Principal 객체로 받기
            @RequestBody @Valid OAuth2RegisterRequest req
    ) {
        JwtAuthFilter.JwtPrincipal jwtPrincipal = (JwtAuthFilter.JwtPrincipal) authentication.getPrincipal();

        // 2. getter 메서드로 깔끔하게 ID 가져오기
        Long userId = jwtPrincipal.userId();

        log.info("소셜 회원 추가 정보 입력 시도: userId={}, name={}", userId, req.userName());

        auth.registerOAuth2User(userId, req);

        return ResponseEntity.ok().build();
    }
}

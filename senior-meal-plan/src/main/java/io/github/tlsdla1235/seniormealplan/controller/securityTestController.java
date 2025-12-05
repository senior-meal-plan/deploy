package io.github.tlsdla1235.seniormealplan.controller;


import io.github.tlsdla1235.seniormealplan.dto.auth.AuthResponse;
import io.github.tlsdla1235.seniormealplan.dto.auth.LoginRequest;
import io.github.tlsdla1235.seniormealplan.dto.auth.RefreshRequest;
import io.github.tlsdla1235.seniormealplan.dto.auth.RegisterRequest;
import io.github.tlsdla1235.seniormealplan.dto.oauth.OAuth2RegisterRequest;
import io.github.tlsdla1235.seniormealplan.service.user.AuthService;
import io.github.tlsdla1235.seniormealplan.service.user.UserTopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class securityTestController {
    private final AuthService auth;
    private final UserTopicService userSelectedTopicService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        try {
            auth.register(req);   // 모든 값이 req에 들어옴
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(409).body(Map.of("error", "데이터 제약조건 위반"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req){
        LocalDate date = LocalDate.now();
        log.info("date: {}", date);
        return ResponseEntity.ok(auth.login(req));
    }

    // ▼ 3. Refresh 엔드포인트 (신규) ▼
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest req) {
        try {
            AuthResponse response = auth.refresh(req.refreshToken());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | SecurityException e) {
            log.warn("Refresh 실패: {}", e.getMessage());
            // 유효하지 않은 토큰, Redis에 없거나 불일치 시 401 응답
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }


}

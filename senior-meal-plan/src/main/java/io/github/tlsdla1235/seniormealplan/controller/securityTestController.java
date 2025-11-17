package io.github.tlsdla1235.seniormealplan.controller;


import io.github.tlsdla1235.seniormealplan.dto.auth.AuthResponse;
import io.github.tlsdla1235.seniormealplan.dto.auth.LoginRequest;
import io.github.tlsdla1235.seniormealplan.dto.auth.RegisterRequest;
import io.github.tlsdla1235.seniormealplan.service.user.AuthService;
import io.github.tlsdla1235.seniormealplan.service.user.UserTopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
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


}

package io.github.tlsdla1235.seniormealplan.controller;

import io.github.tlsdla1235.seniormealplan.config.JwtAuthFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class helloController {

    @PreAuthorize("hasRole('USER')")   // ← ROLE_USER만 접근 OK
    @GetMapping("/api/hello")
    public String hello() {
        return "hello world";
    }

    @GetMapping("/me")
    public String me(@AuthenticationPrincipal JwtAuthFilter.JwtPrincipal me) {
        // 인증 안 됐으면 me == null 이므로 NPE 주의
        if (me == null) return "anonymous";

        Long userId   = me.userId();        // ← Integer user_id
        String  inputId  = me.userInputId();   // ← user_input_id
        String  role     = me.role();          // ← "USER" 등
        return "uid=" + userId + ", uin=" + inputId + ", role=" + role;
    }

    // 권한 예시
    @GetMapping("/hello")
    @PreAuthorize("hasRole('USER')")
    public String hello(@AuthenticationPrincipal JwtAuthFilter.JwtPrincipal me) {
        return "hello, uid=" + me.userId();
    }


}

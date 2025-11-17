package io.github.tlsdla1235.seniormealplan.config;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;                 // ★ Key 말고 이걸 사용

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import io.jsonwebtoken.Claims;



@Component
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;  // 256비트 이상 권장 (Base64)
    @Value("${jwt.exp-minutes:60}")
    private long expMinutes;
    @Value("${jwt.rt-exp-days:7}")
    private long rtExpDays;

    private SecretKey secretKey;

    @PostConstruct
    void init() { secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)); }

    public String generateToken(String subject, Map<String,Object> claims) {
        var now = Instant.now();
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expMinutes, ChronoUnit.MINUTES)))
                .signWith(secretKey)                 // ← 캐시 사용
                .compact();
    }

    public String extractSubject(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)               // ← 캐시 사용
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public Integer extractSubAsUserId(String token) {
        return Integer.valueOf(extractSubject(token));
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)   // 0.12.x API
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUid(String token) {
        Object v = parseClaims(token).get("uid");
        if (v == null) return null;
        if (v instanceof Long i) return i;
        return Long.valueOf(String.valueOf(v));
    }

    public String extractUin(String token) {
        Object v = parseClaims(token).get("uin");
        return v == null ? null : String.valueOf(v);
    }

    public String extractRole(String token) {
        Object v = parseClaims(token).get("role");
        return v == null ? null : String.valueOf(v);
    }

    public boolean isValid(String token) {
        try {
            var payload = parseClaims(token); // 서명 검증 포함
            return payload.getExpiration().after(new Date());
        } catch (io.jsonwebtoken.JwtException e) {
            return false; // 변조/만료 등
        }
    }

}

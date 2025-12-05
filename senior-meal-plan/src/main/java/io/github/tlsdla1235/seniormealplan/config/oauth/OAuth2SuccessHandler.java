package io.github.tlsdla1235.seniormealplan.config.oauth;

import io.github.tlsdla1235.seniormealplan.config.JwtService;
import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.rt-exp-days:7}")
    private long rtExpDays;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 1. 카카오 ID 추출 및 유저 조회
        String providerId = String.valueOf(attributes.get("id"));
        String userInputId = "kakao_" + providerId;

        User user = userRepository.findByUserInputId(userInputId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String userIdStr = String.valueOf(user.getUserId());

        // 2. Access Token 생성 (AuthService와 동일한 메서드 사용)
        String accessToken = jwtService.generateAccessToken(
                userIdStr,
                Map.of(
                        "uid", user.getUserId(),
                        "uin", user.getUserInputId(),
                        "role", user.getRole().name()
                )
        );

        // 3. Refresh Token 생성 (AuthService와 동일)
        String refreshToken = jwtService.generateRefreshToken(userIdStr);

        // 4. Redis에 Refresh Token 저장 (★ AuthService 로직 복사)
        ValueOperations<String, String> vOps = redisTemplate.opsForValue();
        String redisKey = "RT:" + userIdStr; // 예: "RT:1"

        // 만료 시간 설정하여 저장
        vOps.set(redisKey, refreshToken, rtExpDays, TimeUnit.DAYS);

        log.info("OAuth2 로그인 성공 & Redis RT 저장: Key={}, UserID={}", redisKey, userIdStr);

        log.info("=========================================================");
        log.info("OAuth2 로그인 성! 토큰들은");
        log.info("Access Token: {}", accessToken);
        log.info("Refresh Token: {}", refreshToken);
        log.info("=========================================================");
        
        // 5. 프론트엔드로 리다이렉트 (AT와 RT 모두 전달)
        // 상황에 따라 주소를 선택하세요:
        // - 웹 개발 테스트: "http://localhost:3000/oauth/callback"
        // - 앱 개발 테스트: "seniormeal://oauth/callback"
        String targetUrl = UriComponentsBuilder.fromUriString("seniormeal://oauth/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("role", user.getRole().name())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
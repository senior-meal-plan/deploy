package io.github.tlsdla1235.seniormealplan.service.user;

import io.github.tlsdla1235.seniormealplan.config.JwtService;
import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.Role;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.TopicType;
import io.github.tlsdla1235.seniormealplan.domain.preference.HealthTopic;
import io.github.tlsdla1235.seniormealplan.domain.preference.UserSelectedTopic;
import io.github.tlsdla1235.seniormealplan.domain.recipe.Recipe;
import io.github.tlsdla1235.seniormealplan.domain.recipe.UserWeeklyRecommendation;
import io.github.tlsdla1235.seniormealplan.dto.auth.AuthResponse;
import io.github.tlsdla1235.seniormealplan.dto.auth.LoginRequest;
import io.github.tlsdla1235.seniormealplan.dto.auth.RegisterRequest;
import io.github.tlsdla1235.seniormealplan.dto.oauth.OAuth2RegisterRequest;
import io.github.tlsdla1235.seniormealplan.repository.HealthTopicRepository;
import io.github.tlsdla1235.seniormealplan.repository.UserRepository;
import io.github.tlsdla1235.seniormealplan.repository.UserSelectedTopicRepository;
import io.github.tlsdla1235.seniormealplan.repository.recipe.RecipeRepository;
import io.github.tlsdla1235.seniormealplan.repository.recipe.UserWeeklyRecommendationRepository;
import io.github.tlsdla1235.seniormealplan.service.recipe.RecipeRecommendService;
import lombok.Getter;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final UserSelectedTopicRepository userSelectedRepo;
    private final HealthTopicRepository healthRepo;
    private final HealthMetricService healthMetricService;
    private final RecipeRecommendService recipeRecommendService;


    private final RedisTemplate<String, String> redisTemplate;
    @Value("${jwt.rt-exp-days:7}")
    private long rtExpDays;

    @Transactional
    public void register(RegisterRequest req) {
        if (repo.existsByUserInputId(req.userInputId())) {
            throw new IllegalArgumentException("이미 가입된 사용자 ID");
        }
        log.info(String.valueOf(req.userSelectedTopic()));

        var u = new io.github.tlsdla1235.seniormealplan.domain.User();
        u.setUserInputId(req.userInputId());
        u.setPassword(encoder.encode(req.password()));
        u.setUserName(req.userName());
        u.setRole(Role.USER);                 // ★ 클라이언트 전달값 사용
        u.setUserGender(req.userGender());     // ★ 클라이언트 전달값 사용
        u.setUserHeight(req.userHeight());
        u.setUserWeight(req.userWeight());
        u.setAge(req.userAge());
        repo.save(u); // createdAt은 @PrePersist가 채움

        List<String> topicNames = req.userSelectedTopic();
        List<HealthTopic> foundTopics = new ArrayList<>();

        if (topicNames != null && !topicNames.isEmpty()) {
            foundTopics = healthRepo.findByNameIn(topicNames);

            if (foundTopics.size() != topicNames.size()) {
                final List<HealthTopic> ft = foundTopics; // ✅ 람다용 final 참조
                List<String> notFoundNames = topicNames.stream()
                        .filter(name -> ft.stream().noneMatch(t -> t.getName().equals(name)))
                        .toList();
                throw new IllegalArgumentException("존재하지 않는 건강 토픽이 포함되어 있습니다: " + notFoundNames);
            }
            List<UserSelectedTopic> topicsToSave = foundTopics.stream()
                    .map(topic -> new UserSelectedTopic(u, topic))
                    .toList();
            userSelectedRepo.saveAll(topicsToSave);
            log.info("사용자 '{}'의 건강 토픽 {}개가 성공적으로 저장되었습니다.", u.getUserInputId(), topicsToSave.size());
        }
        recipeRecommendService.generateInitialRecommendations(u, foundTopics);
        healthMetricService.init_HealthMetricService(u);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        // 인증 시도 (ID/패스워드)
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.userInputId(), req.password())
        );

        // 유저 조회
        User u = repo.findByUserInputId(req.userInputId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        String userIdStr = String.valueOf(u.getUserId());

        // Access Token 생성
        String accessToken = jwt.generateAccessToken(
                userIdStr,
                Map.of(
                        "uid",  u.getUserId(),
                        "uin",  u.getUserInputId(),
                        "role", u.getRole().name()
                )
        );

        // Refresh Token 생성
        String refreshToken = jwt.generateRefreshToken(userIdStr);

        // Redis에 Refresh Token 저장
        ValueOperations<String, String> vOps = redisTemplate.opsForValue();
        String redisKey = "RT:" + userIdStr; // 예: "RT:1"
        vOps.set(redisKey, refreshToken, rtExpDays, TimeUnit.DAYS); // 만료시간 동시 설정

        log.info("Redis에 RT 저장: Key={}, UserID={}", redisKey, userIdStr);

        // AT, RT 모두 반환
        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refresh(String refreshToken) {
        // 1. RT 검증
        if (refreshToken == null || !jwt.isValid(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        //  RT에서 UserId 추출
        String userIdStr = jwt.extractSubject(refreshToken);
        String redisKey = "RT:" + userIdStr;

        // Redis에 저장된 RT 조회
        ValueOperations<String, String> vOps = redisTemplate.opsForValue();
        String storedRt = vOps.get(redisKey);

        // Redis의 RT와 비교
        if (storedRt == null) {
            throw new SecurityException("Refresh Token이 Redis에 존재하지 않습니다. (로그아웃됨)");
        }
        if (!storedRt.equals(refreshToken)) {
            throw new SecurityException("Refresh Token이 일치하지 않습니다. (탈취 의심)");
        }
        User u = repo.findById(Long.parseLong(userIdStr))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        //새 AT 생성 (RT는 재사용 또는 재발급 - 여기선 재발급(Rotation)
        String newAccessToken = jwt.generateAccessToken(
                userIdStr,
                Map.of(
                        "uid",  u.getUserId(),
                        "uin",  u.getUserInputId(),
                        "role", u.getRole().name()
                )
        );

        // ★ 7. (보안강화) Refresh Token Rotation: RT도 새로 발급
        String newRefreshToken = jwt.generateRefreshToken(userIdStr);
        vOps.set(redisKey, newRefreshToken, rtExpDays, TimeUnit.DAYS); // Redis에 새 RT 덮어쓰기

        log.info("RT 갱신 완료: UserID={}", userIdStr);

        // 8. 새 토큰 세트 반환
        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void registerOAuth2User(Long userId, OAuth2RegisterRequest req) {
        User u = repo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // 정보 업데이트
        u.setUserName(req.userName());
        u.setAge(req.userAge());
        u.setUserHeight(req.userHeight());
        u.setUserWeight(req.userWeight());
        u.setUserGender(req.userGender());
        u.setRole(Role.USER);


        List<String> topicNames = req.userSelectedTopic();
        List<HealthTopic> foundTopics = new ArrayList<>();
        if (topicNames != null && !topicNames.isEmpty()) {
            foundTopics = healthRepo.findByNameIn(topicNames);

            if (foundTopics.size() != topicNames.size()) {
                final List<HealthTopic> ft = foundTopics; // ✅ 람다용 final 참조
                List<String> notFoundNames = topicNames.stream()
                        .filter(name -> ft.stream().noneMatch(t -> t.getName().equals(name)))
                        .toList();
                throw new IllegalArgumentException("존재하지 않는 건강 토픽이 포함되어 있습니다: " + notFoundNames);
            }
            List<UserSelectedTopic> topicsToSave = foundTopics.stream()
                    .map(topic -> new UserSelectedTopic(u, topic))
                    .toList();
            userSelectedRepo.saveAll(topicsToSave);
            log.info("사용자 '{}'의 건강 토픽 {}개가 성공적으로 저장되었습니다.", u.getUserInputId(), topicsToSave.size());
        }
        recipeRecommendService.generateInitialRecommendations(u, foundTopics);
    }

}

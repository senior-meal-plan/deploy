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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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

    public AuthResponse login(LoginRequest req) {
        // 인증 시도 (ID/패스워드)
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.userInputId(), req.password())
        );

        // 토큰 발급
        User u = repo.findByUserInputId(req.userInputId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자"));

        String token = jwt.generateToken(
                String.valueOf(u.getUserId()),
                Map.of(
                        "uid",  u.getUserId(),
                        "uin",  u.getUserInputId(),
                        "role", u.getRole().name()
                )
        );
        return new AuthResponse(token);
    }



}

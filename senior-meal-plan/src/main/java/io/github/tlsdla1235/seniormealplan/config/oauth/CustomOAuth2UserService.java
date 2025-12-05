package io.github.tlsdla1235.seniormealplan.config.oauth;

import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.Role;
import io.github.tlsdla1235.seniormealplan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 1. 카카오 로그인 정보 추출
        String provider = userRequest.getClientRegistration().getRegistrationId(); // "kakao"
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String providerId = String.valueOf(attributes.get("id")); // 식별자

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String nickname = (String) profile.get("nickname");

        // 2. DB 조회용 ID 생성 (kakao_123456)
        String userInputId = provider + "_" + providerId;

        // 3. 유저가 없으면 GUEST로 저장, 있으면 불러오기
        User user = userRepository.findByUserInputId(userInputId)
                .orElseGet(() -> createUser(userInputId, nickname, provider, providerId));

        // 4. 리턴 (여기서 리턴된 정보가 SuccessHandler로 넘어감)
        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    private User createUser(String userInputId, String nickname, String provider, String providerId) {
        return userRepository.save(User.builder()
                .userInputId(userInputId)
                .userName(nickname)
                .password(UUID.randomUUID().toString()) // 임시 비밀번호
                .role(Role.GUEST) // ★ 중요: 아직 추가 정보 입력 전이므로 GUEST
                .provider(provider)
                .providerId(providerId)
                // age, height, weight 등은 null로 저장됨
                .build());
    }
}
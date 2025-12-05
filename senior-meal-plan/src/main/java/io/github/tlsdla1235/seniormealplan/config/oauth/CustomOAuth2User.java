package io.github.tlsdla1235.seniormealplan.config.oauth;

import io.github.tlsdla1235.seniormealplan.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User {

    private final User user; // 우리 DB의 유저 정보
    private final Map<String, Object> attributes; // 카카오에서 받은 원본 데이터

    public CustomOAuth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 우리 DB의 Role(USER, GUEST)을 스프링 시큐리티 권한으로 변환
        return Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()));
    }

    @Override
    public String getName() {
        // 카카오 유저 식별자 (예: 123456789)
        return String.valueOf(attributes.get("id"));
    }

    // 편의 메서드: 우리 DB의 PK(userId) 반환
    public Long getUserId() {
        return user.getUserId();
    }
}
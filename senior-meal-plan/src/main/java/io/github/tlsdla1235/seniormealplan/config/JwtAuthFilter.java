package io.github.tlsdla1235.seniormealplan.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserDetailsService uds;

    public JwtAuthFilter(JwtService j, UserDetailsService u){ this.jwt=j; this.uds=u; }
    public static record JwtPrincipal(Long userId, String userInputId, String role) {}

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        // 이미 인증되어 있으면 스킵
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(req, res);
            return;
        }

        String auth = req.getHeader("Authorization");
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                // 1) 서명/만료 검증
                if (!jwt.isValid(token)) {
                    chain.doFilter(req, res);
                    return;
                }

                // 2) 클레임 추출
                Long uid = jwt.extractUid(token);     // Integer user_id
                String  uin = jwt.extractUin(token);     // user_input_id
                String  role = jwt.extractRole(token);   // 예: "USER"

                // 3) 권한/프린시펄 구성
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                var principal   = new JwtPrincipal(uid, uin, role);

                // 4) SecurityContext에 인증 세팅
                var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception ignore) {
                // 토큰 문제면 인증 없이 통과 → 이후 Security가 401/403 처리
            }
        }

        chain.doFilter(req, res);
    }
}

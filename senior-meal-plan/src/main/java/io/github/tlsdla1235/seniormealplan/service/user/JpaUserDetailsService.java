package io.github.tlsdla1235.seniormealplan.service.user;

import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class JpaUserDetailsService implements UserDetailsService {
    private final UserRepository repo;
    public JpaUserDetailsService(UserRepository repo) { this.repo = repo; }

    @Override
    @Cacheable(value = "userDetails", key = "#userInputId")
    public UserDetails loadUserByUsername(String userInputId) throws UsernameNotFoundException {
        User u = repo.findByUserInputId(userInputId).orElseThrow(() -> new UsernameNotFoundException(userInputId));
        Collection<GrantedAuthority> auth = List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()));
        return new org.springframework.security.core.userdetails.User(u.getUserInputId(), u.getPassword(), auth);
    }
}

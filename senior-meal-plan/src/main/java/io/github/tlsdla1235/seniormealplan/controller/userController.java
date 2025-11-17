package io.github.tlsdla1235.seniormealplan.controller;

import io.github.tlsdla1235.seniormealplan.config.JwtAuthFilter;
import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.dto.user.UpdateUserDto;
import io.github.tlsdla1235.seniormealplan.dto.user.WhoAmIDto;
import io.github.tlsdla1235.seniormealplan.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/user")
public class userController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<WhoAmIDto> whoamI(@AuthenticationPrincipal JwtAuthFilter.JwtPrincipal me) {
        User u = User.builder().userId(me.userId()).build();
        WhoAmIDto whoAmIDto = userService.whoAmI(u);
        return ResponseEntity.ok(whoAmIDto);
    }

    @PatchMapping("/me")
    public ResponseEntity<String> updateUser(@AuthenticationPrincipal JwtAuthFilter.JwtPrincipal me, @RequestBody UpdateUserDto updateUserDto) {
        User u = User.builder().userId(me.userId()).build();
        userService.updateUserProfile(u, updateUserDto);
        return ResponseEntity.ok("success");
    }
}

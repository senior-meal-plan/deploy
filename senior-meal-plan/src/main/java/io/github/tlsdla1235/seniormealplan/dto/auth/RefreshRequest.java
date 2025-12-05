package io.github.tlsdla1235.seniormealplan.dto.auth;

import jakarta.validation.constraints.NotEmpty;

public record RefreshRequest(
        @NotEmpty String refreshToken
) {
}

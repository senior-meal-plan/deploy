package io.github.tlsdla1235.seniormealplan.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String userInputId,
        @NotBlank String password
) {}
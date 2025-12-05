package io.github.tlsdla1235.seniormealplan.dto.oauth;

import io.github.tlsdla1235.seniormealplan.domain.enumPackage.UserGenderType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record OAuth2RegisterRequest(
        @NotBlank @Size(min = 1, max = 50)
        String userName,

        @NotNull
        UserGenderType userGender,

        @NotNull @DecimalMin("0.0") @Digits(integer = 3, fraction = 2)
        BigDecimal userHeight,

        @NotNull @DecimalMin("0.0") @Digits(integer = 3, fraction = 2)
        BigDecimal userWeight,

        @NotNull
        Integer userAge,

        @NotNull
        List<String> userSelectedTopic
) {}
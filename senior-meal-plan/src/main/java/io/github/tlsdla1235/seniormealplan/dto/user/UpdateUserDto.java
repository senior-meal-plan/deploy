package io.github.tlsdla1235.seniormealplan.dto.user;

import io.github.tlsdla1235.seniormealplan.domain.enumPackage.UserGenderType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record UpdateUserDto(
        @NotBlank @Size(min = 1, max = 50)
        String userName,

        @NotNull
        UserGenderType userGender,       // ★ 필수 (Enum 상수로)

        @NotNull @DecimalMin("0.0") @Digits(integer = 3, fraction = 2)
        BigDecimal userHeight,           // ★ 필수 (예: 170.5)

        @NotNull @DecimalMin("0.0") @Digits(integer = 3, fraction = 2)
        BigDecimal userWeight,            // ★ 필수 (예: 65.0)

        @NotNull
        Integer userAge,

        @NotNull
        List<String> userSelectedTopic
) {
}

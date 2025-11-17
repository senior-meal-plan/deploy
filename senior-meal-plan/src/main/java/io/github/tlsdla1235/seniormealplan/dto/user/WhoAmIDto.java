package io.github.tlsdla1235.seniormealplan.dto.user;

import io.github.tlsdla1235.seniormealplan.domain.User;

import java.math.BigDecimal;
import java.util.List;
import java.io.Serializable;

public record WhoAmIDto (
        Long userId,
        String userName,
        Integer age,
        BigDecimal userHeight,
        BigDecimal userWeight,
        String Gender,
        List<UserTopicDto> topics
)implements Serializable{
    public static WhoAmIDto from(User user, List<UserTopicDto> topics) {
        return new WhoAmIDto(
                user.getUserId(),
                user.getUserName(),
                user.getAge(),
                user.getUserHeight(),
                user.getUserWeight(),
                user.getUserGender().name(),
                topics
        );
    }
}

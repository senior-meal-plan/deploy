package io.github.tlsdla1235.seniormealplan.domain.preference.legacy;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "health_goal_input_by_user")
@Getter
@NoArgsConstructor
@DiscriminatorValue("GOAL")
public class HealthGoalInputByUser extends UserPreference{
    @Column(name = "goal_type", nullable = false)
    private String goalType;
}

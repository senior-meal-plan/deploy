package io.github.tlsdla1235.seniormealplan.domain.preference.legacy;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "food_restrictions")
@Getter
@NoArgsConstructor
@DiscriminatorValue("RESTRICTION")
public class FoodRestrictions extends UserPreference{
    @Column(name = "food_type", nullable = false)
    private String foodType;
}

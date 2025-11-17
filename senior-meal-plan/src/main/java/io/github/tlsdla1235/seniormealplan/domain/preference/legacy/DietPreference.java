package io.github.tlsdla1235.seniormealplan.domain.preference.legacy;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "diet_preferences")
@Getter
@NoArgsConstructor
@DiscriminatorValue("DIET") // pref_type이 'DIET'인 경우 이 엔티티와 매핑
public class DietPreference extends UserPreference {

    @Column(name = "diet_type", nullable = false)
    private String dietType;
}

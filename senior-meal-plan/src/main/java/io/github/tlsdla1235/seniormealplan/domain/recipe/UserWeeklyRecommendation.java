package io.github.tlsdla1235.seniormealplan.domain.recipe;

import io.github.tlsdla1235.seniormealplan.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "user_weekly_recommendations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserWeeklyRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Long recommendationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @Column(name = "generated_at")
    private LocalDate generatedAt;

    public UserWeeklyRecommendation(User user, Recipe recipe, LocalDate generatedAt) {
        this.user = user;
        this.recipe = recipe;
        this.generatedAt = generatedAt;
    }
}
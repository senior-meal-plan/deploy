package io.github.tlsdla1235.seniormealplan.domain.recipe;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "recipe_recipe_types")
@Getter
@Setter
@NoArgsConstructor
public class RecipeRecipeType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id")
    private RecipeType recipeType;

    @Column(name = "tagged_by")
    private String taggedBy;

    @Column(name = "tagged_at")
    private LocalDateTime taggedAt;

    @PrePersist
    public void prePersist() {
        this.taggedAt = LocalDateTime.now();
    }
}
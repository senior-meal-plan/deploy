package io.github.tlsdla1235.seniormealplan.domain.recipe;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "recipe_steps")
@Getter
@Setter
@NoArgsConstructor
public class RecipeStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "step_id")
    private Long stepId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @Column(name = "step_no", nullable = false)
    private Long stepNo;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String instruction;

    @Column(name = "image_url")
    private String imageUrl;
}

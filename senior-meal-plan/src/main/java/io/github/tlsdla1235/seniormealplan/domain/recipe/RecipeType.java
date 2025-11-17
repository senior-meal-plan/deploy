package io.github.tlsdla1235.seniormealplan.domain.recipe;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "recipe_types")
@Getter
@Setter
@NoArgsConstructor
public class RecipeType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "type_id")
    private Long typeId;

    @Column(name = "type_name", nullable = false)
    private String typeName;

    @Lob
    @Column(name = "description")
    private String description;
}
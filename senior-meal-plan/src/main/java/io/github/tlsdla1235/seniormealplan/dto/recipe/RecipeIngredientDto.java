package io.github.tlsdla1235.seniormealplan.dto.recipe;

import io.github.tlsdla1235.seniormealplan.domain.recipe.RecipeIngredient;

public record RecipeIngredientDto(
        Long id,
        String ingredients
) {
    public static RecipeIngredientDto from(RecipeIngredient entity) {
        return new RecipeIngredientDto(
                entity.getId(),
                entity.getIngredients()
        );
    }
}

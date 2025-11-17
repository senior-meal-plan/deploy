package io.github.tlsdla1235.seniormealplan.dto.recipe;

import io.github.tlsdla1235.seniormealplan.domain.recipe.Recipe;

public record RecipeGenerateDto(
        String name,
        String description,
        String difficultly,
        String imageUrl
) {
    public static RecipeGenerateDto from(Recipe recipe)
    {
        return new RecipeGenerateDto(
                recipe.getName(),
                recipe.getDescription(),
                recipe.getDifficulty().name(),
                recipe.getImageUrl()
        );
    }
}

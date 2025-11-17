package io.github.tlsdla1235.seniormealplan.dto.recipe;

import io.github.tlsdla1235.seniormealplan.domain.recipe.Recipe;
import io.github.tlsdla1235.seniormealplan.domain.recipe.RecipeIngredient;

import java.util.List;

public record RecipeResponseDto(
        Long recipeId,
        String name,
        String description,
        String difficultly,
        String imageUrl,
        List<String> ingredients
) {
    public static RecipeResponseDto from(Recipe recipe,List<RecipeIngredient> ingredients)
    {
        List<String> ingredientNames = ingredients.stream()
                .map(RecipeIngredient::getIngredients)
                .toList();

        return new RecipeResponseDto(
                recipe.getRecipeId(),
                recipe.getName(),
                recipe.getDescription(),
                "",
                recipe.getImageUrl(),
                ingredientNames
        );
    }
}

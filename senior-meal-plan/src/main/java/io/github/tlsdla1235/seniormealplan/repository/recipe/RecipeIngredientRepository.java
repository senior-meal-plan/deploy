package io.github.tlsdla1235.seniormealplan.repository.recipe;

import io.github.tlsdla1235.seniormealplan.domain.recipe.Recipe;
import io.github.tlsdla1235.seniormealplan.domain.recipe.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {
    List<RecipeIngredient> findByRecipeIn(List<Recipe> recipes);

    List<RecipeIngredient> findByRecipe_RecipeId(Long recipeId);
}

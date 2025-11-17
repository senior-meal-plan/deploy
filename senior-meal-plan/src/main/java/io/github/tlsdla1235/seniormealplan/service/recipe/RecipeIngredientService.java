package io.github.tlsdla1235.seniormealplan.service.recipe;

import io.github.tlsdla1235.seniormealplan.domain.recipe.Recipe;
import io.github.tlsdla1235.seniormealplan.domain.recipe.RecipeIngredient;
import io.github.tlsdla1235.seniormealplan.repository.recipe.RecipeIngredientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeIngredientService {
    private final RecipeIngredientRepository recipeIngredientRepository;

    public List<RecipeIngredient> getRecipeIngredientsListIn(List<Recipe> recipes)
    {
        return recipeIngredientRepository.findByRecipeIn(recipes);
    }
}

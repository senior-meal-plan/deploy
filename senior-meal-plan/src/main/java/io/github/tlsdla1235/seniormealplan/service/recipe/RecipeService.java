package io.github.tlsdla1235.seniormealplan.service.recipe;

import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.recipe.Recipe;
import io.github.tlsdla1235.seniormealplan.domain.recipe.RecipeIngredient;
import io.github.tlsdla1235.seniormealplan.domain.recipe.RecipeStep;
import io.github.tlsdla1235.seniormealplan.dto.recipe.RecipeGenerateDto;
import io.github.tlsdla1235.seniormealplan.dto.recipe.RecipeIngredientDto;
import io.github.tlsdla1235.seniormealplan.dto.recipe.RecipeResponseDto;
import io.github.tlsdla1235.seniormealplan.dto.recipe.RecipeStepDto;
import io.github.tlsdla1235.seniormealplan.repository.recipe.RecipeIngredientRepository;
import io.github.tlsdla1235.seniormealplan.repository.recipe.RecipeRepository;
import io.github.tlsdla1235.seniormealplan.repository.recipe.RecipeStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final RecipeIngredientService recipeIngredientService;
    private final UserBookMarkedRecipeService userBookMarkedRecipeService;
    private final RecipeRecommendService recipeRecommendService;
    private final RecipeStepRepository recipeStepRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;

    @Transactional
    public void save(RecipeGenerateDto dto) {
        Recipe newRecipe = Recipe.builder().name(dto.name()).description(dto.description()).build();
        log.info("save recipe: {}", newRecipe);
    }


    public List<RecipeResponseDto> getRecipesByRecommendForUser(User user)
    {
        List<RecipeResponseDto> result = getRecipesForUser(recipeRecommendService.findWeeklyRecommendationsByUser(user));
        log.info("사용자 id:{}에 대한 getRecipesForRecommendForUser 조회 결과:{}", user.getUserId(), result);
        return result;
    }

    public List<RecipeResponseDto> getRecipesByBookmarkForUser(User user)
    {
        List<RecipeResponseDto> result = getRecipesForUser(userBookMarkedRecipeService.findUserBookmarkRecipeByUser(user));
        log.info("사용자 id:{}에 대한 getRecipesByBookmarkForUser 조회 결과:{}", user.getUserId(), result);
        return result;
    }

    /**
     * 위의 함수 두개에서 사용하기 위해 정의한 함수
     * @param recipes
     * @return
     */
    public List<RecipeResponseDto> getRecipesForUser(List<Recipe> recipes) {
        if (recipes.isEmpty()) {
            return Collections.emptyList();
        }
        List<Recipe> perRecipes = recipeRepository.findAllById(recipes.stream().map(Recipe::getRecipeId).toList());
        List<RecipeIngredient> ingredients = recipeIngredientService.getRecipeIngredientsListIn(perRecipes);

        Map<Recipe, List<RecipeIngredient>> ingredientsMap = ingredients.stream()
                .collect(Collectors.groupingBy(RecipeIngredient::getRecipe));

        List<RecipeResponseDto> dtos = perRecipes.stream()
                .map(recipe -> {
                    List<RecipeIngredient> ingredientsForThisRecipe = ingredientsMap.getOrDefault(recipe, Collections.emptyList());

                    return RecipeResponseDto.from(recipe, ingredientsForThisRecipe);
                })
                .toList();
        return dtos;
    }


    public List<RecipeStepDto> getRecipeSteps(Recipe recipe) {
        List<RecipeStep> steps = recipeStepRepository.findByRecipe_RecipeIdOrderByStepNoAsc(recipe.getRecipeId());
        List<RecipeStepDto> dtos = steps.stream().map(RecipeStepDto::from).collect(Collectors.toList());
        log.info("recipe id:{}에 대한 레시피 순서 조회:{}", recipe.getRecipeId(), dtos);
        return dtos;
    }

    public List<RecipeIngredientDto> getRecipeIngredients(Long recipeId) {
        // Repository를 사용하여 recipeId로 재료 목록 조회
        List<RecipeIngredient> ingredients = recipeIngredientRepository.findByRecipe_RecipeId(recipeId);

        // Entity 리스트를 DTO 리스트로 변환
        List<RecipeIngredientDto> dtos = ingredients.stream()
                .map(RecipeIngredientDto::from)
                .collect(Collectors.toList());

        log.info("recipe id:{}에 대한 레시피 재료 조회:{}", recipeId, dtos);
        return dtos;
    }

}
package io.github.tlsdla1235.seniormealplan.controller.recipe;

import io.github.tlsdla1235.seniormealplan.config.JwtAuthFilter;
import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.recipe.Recipe;
import io.github.tlsdla1235.seniormealplan.dto.recipe.RecipeIdDto;
import io.github.tlsdla1235.seniormealplan.dto.recipe.RecipeIngredientDto;
import io.github.tlsdla1235.seniormealplan.dto.recipe.RecipeResponseDto;
import io.github.tlsdla1235.seniormealplan.dto.recipe.RecipeStepDto;
import io.github.tlsdla1235.seniormealplan.service.recipe.RecipeService;
import io.github.tlsdla1235.seniormealplan.service.recipe.UserBookMarkedRecipeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/v1/recipe")
public class RecipeController {
    private final RecipeService recipeService;
    private final UserBookMarkedRecipeService userBookMarkedRecipeService;

    @GetMapping("/recommended")
    public ResponseEntity<List<RecipeResponseDto>> getRecommended(@AuthenticationPrincipal JwtAuthFilter.JwtPrincipal me) {
        List<RecipeResponseDto> response = recipeService.getRecipesByRecommendForUser(User.builder().userId(me.userId()).build());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/bookmarked")
    public ResponseEntity<List<RecipeResponseDto>> getBookmarked(@AuthenticationPrincipal JwtAuthFilter.JwtPrincipal me) {
        List<RecipeResponseDto> response = recipeService.getRecipesByBookmarkForUser(User.builder().userId(me.userId()).build());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bookmarking")
    public ResponseEntity<String> addBookmark(@AuthenticationPrincipal JwtAuthFilter.JwtPrincipal me, @RequestBody RecipeIdDto recipeId) {
        userBookMarkedRecipeService.addUserBookmarkRecipe(User.builder().userId(me.userId()).build(), Recipe.builder().recipeId(recipeId.recipeId()).build());
        return ResponseEntity.ok("Bookmarked recipe successfully added");
    }

    @DeleteMapping("/bookmarking")
    public ResponseEntity<String> removeBookmark(@AuthenticationPrincipal JwtAuthFilter.JwtPrincipal me, @RequestBody RecipeIdDto recipeId) {
        userBookMarkedRecipeService.deleteUserBookmarkRecipe(
                User.builder().userId(me.userId()).build(),
                Recipe.builder().recipeId(recipeId.recipeId()).build()
        );
        return ResponseEntity.ok("Bookmark removed successfully");
    }

    @GetMapping("/{recipeId}/steps")
    public ResponseEntity<List<RecipeStepDto>> getSteps(@PathVariable Long recipeId) {
        List<RecipeStepDto> result = recipeService.getRecipeSteps(Recipe.builder().recipeId(recipeId).build());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{recipeId}/ingredients")
    public ResponseEntity<List<RecipeIngredientDto>> getIngredients(@PathVariable Long recipeId) {
        List<RecipeIngredientDto> result = recipeService.getRecipeIngredients(recipeId);
        return ResponseEntity.ok(result);
    }
}

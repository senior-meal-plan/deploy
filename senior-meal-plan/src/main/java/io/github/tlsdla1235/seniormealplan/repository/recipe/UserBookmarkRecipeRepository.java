package io.github.tlsdla1235.seniormealplan.repository.recipe;

import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.recipe.Recipe;
import io.github.tlsdla1235.seniormealplan.domain.recipe.UserBookmarkedRecipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBookmarkRecipeRepository extends JpaRepository<UserBookmarkedRecipe, Long> {
    Optional<UserBookmarkedRecipe> findByUserAndRecipe(User user, Recipe recipe);
    List<UserBookmarkedRecipe> findByUser(User user);
}

package io.github.tlsdla1235.seniormealplan.dto.recipe;

import io.github.tlsdla1235.seniormealplan.domain.recipe.RecipeStep;

public record RecipeStepDto(
        Long stepNo,
        String instruction,
        String imageUrl
) {
    public static RecipeStepDto from(RecipeStep step) {
        return new RecipeStepDto(
                step.getStepNo(),
                step.getInstruction(),
                step.getImageUrl()
        );
    }
}

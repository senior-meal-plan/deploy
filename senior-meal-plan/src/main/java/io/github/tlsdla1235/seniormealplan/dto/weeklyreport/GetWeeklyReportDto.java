package io.github.tlsdla1235.seniormealplan.dto.weeklyreport;

import io.github.tlsdla1235.seniormealplan.domain.recipe.Recipe;
import io.github.tlsdla1235.seniormealplan.domain.report.WeeklyReport;

import java.time.LocalDate;
import java.util.List;

public record GetWeeklyReportDto(
        Long WeeklyReportId,
        LocalDate weekStart,
        LocalDate weekEnd,
        String summaryGoodPoint,
        String summaryBadPoint,
        String summaryAiRecommand,
        String severity,
        List<RecipeInfo> recommendedRecipes
) {

    public record RecipeInfo(
            Long recipeId,
            String name,
            String description,
            String imageUrl
    ) {
        public static RecipeInfo from(Recipe recipe) {
            return new RecipeInfo(
                    recipe.getRecipeId(),
                    recipe.getName(),
                    recipe.getDescription(),
                    recipe.getImageUrl()
            );
        }
    }

    public static GetWeeklyReportDto of(WeeklyReport weeklyReport, List<Recipe> recipes) {
        // Recipe 엔티티 리스트를 RecipeInfo 레코드 리스트로 변환
        List<RecipeInfo> recipeInfos = recipes.stream()
                .map(RecipeInfo::from)
                .toList();

        return new GetWeeklyReportDto(
                weeklyReport.getReportId(),
                weeklyReport.getWeekStart(),
                weeklyReport.getWeekEnd(),
                weeklyReport.getSummaryGoodPoint(),
                weeklyReport.getSummaryBadPoint(),
                weeklyReport.getSummaryAiRecommand(),
                weeklyReport.getSeverity() != null ? weeklyReport.getSeverity().name() : null,
                recipeInfos
        );
    }
}

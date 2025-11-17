package io.github.tlsdla1235.seniormealplan.service.recipe;

import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.TopicType;
import io.github.tlsdla1235.seniormealplan.domain.preference.HealthTopic;
import io.github.tlsdla1235.seniormealplan.domain.recipe.Recipe;
import io.github.tlsdla1235.seniormealplan.domain.recipe.UserWeeklyRecommendation;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.AnalysisResultDto.WeeklyAnalysisResultDto;
import io.github.tlsdla1235.seniormealplan.repository.recipe.RecipeRepository;
import io.github.tlsdla1235.seniormealplan.repository.recipe.UserWeeklyRecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecipeRecommendService {
    private final UserWeeklyRecommendationRepository userWeeklyRecommendationRepository;
    private final RecipeRepository recipeRepository;

    public List<UserWeeklyRecommendation> createWeeklyRecommendations(User user, WeeklyAnalysisResultDto analysisResult) {
        LocalDate generatedDate = LocalDate.now();
        List<UserWeeklyRecommendation> recommendations = analysisResult.aiRecommendRecipe().stream()
                .map(recipeId -> {
                    Recipe recipeReference = recipeRepository.getReferenceById(recipeId);
                    return new UserWeeklyRecommendation(user, recipeReference, generatedDate);
                })
                .collect(Collectors.toList());

        log.info("사용자 id :{}에 대해 recipe Id:{}들이 추천되었습니다.", user.getUserId(), analysisResult.aiRecommendRecipe());
        return userWeeklyRecommendationRepository.saveAll(recommendations);
    }


    public List<Recipe> findWeeklyRecommendationsByUser(User user) {
        return userWeeklyRecommendationRepository.findMostRecentByUser(user).stream().map(UserWeeklyRecommendation::getRecipe).collect(Collectors.toList());
    }




    public void generateInitialRecommendations(User user, List<HealthTopic> selectedTopics) {
        List<String> healthGoalNames = selectedTopics.stream()
                .filter(ht -> ht.getTopicType() == TopicType.HEALTH_GOAL)
                .map(HealthTopic::getName)
                .toList();

        List<String> allergenNames = selectedTopics.stream()
                .filter(ht -> ht.getTopicType() == TopicType.ALLERGEN)
                .map(HealthTopic::getName)
                .toList();

        Pageable top10 = PageRequest.of(0, 10);

        List<Long> recommendedIds;

        if (!healthGoalNames.isEmpty()) {
            // 건강 목표 있음
            if (!allergenNames.isEmpty()) {
                log.info("추천(Goals=Y, Allergens=Y) 실행. 알러지: {}", allergenNames.size());
                long goalCount = healthGoalNames.size();
                recommendedIds = recipeRepository
                        .findRecommendedRecipeIdsByGoalsExcludingAllergens_AnyGoal(
                                healthGoalNames, allergenNames, top10
                        );
            } else {
                log.info("추천(Goals=Y, Allergens=N) 실행.");
                recommendedIds = recipeRepository.findRecommendedRecipeIdsByGoalsOnly_AnyGoal(
                        healthGoalNames, top10
                );
            }
        } else {
            // 건강 목표 없음
            if (!allergenNames.isEmpty()) {
                log.info("추천(Goals=N, Allergens=Y) 실행. 알러지: {}", allergenNames.size());
                recommendedIds = recipeRepository.findTopRecipeIdsExcludingAllergens_NotExists(
                        allergenNames, top10
                );
            } else {
                log.info("추천(Goals=N, Allergens=N) 실행.");
                recommendedIds = recipeRepository.findTopRecipeIdsNoFilters(top10);
            }
        }

        if (recommendedIds.isEmpty()) {
            log.warn("사용자 '{}' 조건에 맞는 추천 레시피 없음.", user.getUserInputId());
            return;
        }

        LocalDate today = LocalDate.now();
        var weeklyRecs = recommendedIds.stream()
                .map(recipeId -> {
                    Recipe ref = recipeRepository.getReferenceById(recipeId);
                    return new UserWeeklyRecommendation(user, ref, today);
                })
                .toList();

        userWeeklyRecommendationRepository.saveAll(weeklyRecs);
        log.info("사용자 '{}' 초기 추천 레시피 {}개 저장.", user.getUserInputId(), weeklyRecs.size());
    }
}

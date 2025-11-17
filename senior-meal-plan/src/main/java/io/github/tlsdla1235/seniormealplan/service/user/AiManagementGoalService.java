package io.github.tlsdla1235.seniormealplan.service.user;

import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.preference.AiManagementGoal;
import io.github.tlsdla1235.seniormealplan.domain.preference.HealthTopic;
import io.github.tlsdla1235.seniormealplan.dto.user.UserTopicDto;
import io.github.tlsdla1235.seniormealplan.repository.AiManagementGoalRepository;
import io.github.tlsdla1235.seniormealplan.repository.HealthTopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiManagementGoalService {
    private final AiManagementGoalRepository aiManagementGoalRepository;
    private final HealthTopicRepository healthTopicRepository;

    public List<UserTopicDto> getAiSelectedTopics(User user) {
        List<AiManagementGoal> aiGoal = aiManagementGoalRepository.findAllByUserWithHealthTopic(user);
        List<UserTopicDto> userTopicDtos = aiGoal.stream().map(ag->UserTopicDto.fromAi(ag.getHealthTopic())).toList();

        log.info("사용자 id :{} 에 대한 ai가 관리해주는 Topics: {}",user.getUserId(),userTopicDtos);
        return userTopicDtos;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = "whoAmI", key = "#user.userId")
    public void updateAiSelectedTopics(User user, List<String> aiRecommendTopicNames) {
        if (aiRecommendTopicNames == null) {
            aiRecommendTopicNames = List.of();
        }
        log.info("Updating AI Management Goals for user: {}. New topics: {}", user.getUserId(), aiRecommendTopicNames);
        aiManagementGoalRepository.deleteAllByUser(user);
        List<HealthTopic> foundTopics = healthTopicRepository.findByNameIn(aiRecommendTopicNames);
        if (foundTopics.size() != aiRecommendTopicNames.size()) {
            List<String> foundNames = foundTopics.stream().map(HealthTopic::getName).toList();
            List<String> notFound = aiRecommendTopicNames.stream()
                    .filter(name -> !foundNames.contains(name))
                    .toList();
            log.warn("Could not find HealthTopics for names: {} for user: {}", notFound, user.getUserId());
        }
        if (foundTopics.isEmpty()) {
            log.info("No valid topics found or provided for user: {}. All AI goals cleared.", user.getUserId());
            return;
        }
        List<AiManagementGoal> newGoals = foundTopics.stream()
                .map(topic -> AiManagementGoal.builder()
                        .user(user)
                        .healthTopic(topic)
                        .build())
                .collect(Collectors.toList());
        aiManagementGoalRepository.saveAll(newGoals);
        log.info("Successfully set {} new AI Management Goals for user: {}", newGoals.size(), user.getUserId());
        log.info("Cache Evict: AI 토픽 변경, 사용자 id:{}의 'whoAmI' 캐시 삭제", user.getUserId());
    }

}

package io.github.tlsdla1235.seniormealplan.service.user;

import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.preference.AiManagementGoal;
import io.github.tlsdla1235.seniormealplan.domain.preference.HealthTopic;
import io.github.tlsdla1235.seniormealplan.domain.preference.UserSelectedTopic;
import io.github.tlsdla1235.seniormealplan.dto.user.UpdateUserDto;
import io.github.tlsdla1235.seniormealplan.dto.user.UserTopicDto;
import io.github.tlsdla1235.seniormealplan.repository.AiManagementGoalRepository;
import io.github.tlsdla1235.seniormealplan.repository.HealthTopicRepository;
import io.github.tlsdla1235.seniormealplan.repository.UserSelectedTopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserTopicService {
    private final UserSelectedTopicRepository userSelectedTopicRepository;
    private final AiManagementGoalService aiManagementGoalService;
    private final HealthTopicRepository healthRepo;

    public List<UserTopicDto> getUserTopicsFromUser(User user) {
        List<UserSelectedTopic> userSelectedTopics = userSelectedTopicRepository.findAllByUserWithHealthTopic(user);
        List<UserTopicDto> userTopicDtos = userSelectedTopics.stream().map(ut->UserTopicDto.fromUser(ut.getHealthTopic())).toList();
        log.info("사용자 id :{} 에 대한 ai가 관리해주는 Topics: {}",user.getUserId(),userTopicDtos);
        List<UserTopicDto> aiTopicDtos = aiManagementGoalService.getAiSelectedTopics(user);

        List<UserTopicDto> combinedList = Stream.concat(userTopicDtos.stream(), aiTopicDtos.stream())
                .collect(Collectors.toList());
        log.info("사용자 id: {}의 통합 Topics ({}개): {}", user.getUserId(), combinedList.size(), combinedList);
        return combinedList;
    }

    @Transactional
    @CacheEvict(value = "whoAmI", key = "#user.userId")
    public void deleteByUser(User user) {
        Integer deletedTopicNum = userSelectedTopicRepository.deleteByUser(user);
        log.info("user id :{}에 대해 토픽을 {}개 삭제 하였습니다.", user.getUserId(), deletedTopicNum);
    }

    @Transactional
    @CacheEvict(value = "whoAmI", key = "#user.userId")
    public void updateUserTopics(User user, UpdateUserDto req)
    {
        List<String> topicNames = req.userSelectedTopic();
        if (topicNames != null && !topicNames.isEmpty()) {
            List<HealthTopic> foundTopics = healthRepo.findByNameIn(topicNames);

            if (foundTopics.size() != topicNames.size()) {
                List<String> notFoundNames = topicNames.stream()
                        .filter(name -> foundTopics.stream().noneMatch(t -> t.getName().equals(name)))
                        .toList();
                throw new IllegalArgumentException("존재하지 않는 건강 토픽이 포함되어 있습니다: " + notFoundNames);
            }

            // 4-2. UserSelectedTopic 엔티티로 변환 후 저장
            List<UserSelectedTopic> topicsToSave = foundTopics.stream()
                    .map(topic -> new UserSelectedTopic(user, topic))
                    .collect(Collectors.toList());

            userSelectedTopicRepository.saveAll(topicsToSave);
            log.info("userid '{}'의 새 건강 토픽 {}개가 성공적으로 저장되었습니다.", user.getUserId(), topicsToSave.size());
        } else {
            log.info("사용자 '{}'에게 새로 저장할 건강 토픽이 없습니다.", user.getUserId());
        }
    }
}

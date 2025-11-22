package io.github.tlsdla1235.seniormealplan.service.user;

import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.preference.HealthTopic;
import io.github.tlsdla1235.seniormealplan.dto.auth.RegisterRequest;
import io.github.tlsdla1235.seniormealplan.dto.user.UpdateUserDto;
import io.github.tlsdla1235.seniormealplan.dto.user.UserTopicDto;
import io.github.tlsdla1235.seniormealplan.dto.user.WhoAmIDto;
import io.github.tlsdla1235.seniormealplan.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserTopicService userTopicService;
    private final CacheManager cacheManager;

    @Cacheable(value = "whoAmI", key = "#user.userId")
    public WhoAmIDto whoAmI(User user) {
        log.info("Cache Miss: DB에서 사용자 id:{}의 WhoAmI 조회", user.getUserId());
        user = userRepository.findById(user.getUserId()).orElseThrow(EntityNotFoundException::new);
        List<UserTopicDto> userTopic = userTopicService.getUserTopicsFromUser(user);
        WhoAmIDto whoAmI = WhoAmIDto.from(user, userTopic);

        log.info("사용자 id:{}에 대한 WhoAmI함수 호출 결과 :{}", user.getUserId(), whoAmI);

        return whoAmI;
    }

    @Transactional
//    @CacheEvict(value = "whoAmI", key = "#u.userId")
    public User updateUserProfile(User u, UpdateUserDto req) {
        User user = userRepository.findById(u.getUserId()).orElseThrow(EntityNotFoundException::new);
        user.updateProfile(
                req.userName(),
                req.userAge(),
                req.userHeight(),
                req.userWeight(),
                req.userGender()
        );
        log.info("사용자 id'{}'의 기존 건강 토픽을 삭제합니다...", user.getUserId());
        userTopicService.deleteByUser(user);
        log.info("기존 건강 토픽 삭제 완료.");

        userTopicService.updateUserTopics(user, req);

        List<UserTopicDto> newTopics = userTopicService.getUserTopicsFromUser(user);
        WhoAmIDto updatedWhoAmI = WhoAmIDto.from(user, newTopics);

        // Redis 캐시에 직접 PUT (Key: userId, Value: 최신 WhoAmIDto)
        cacheManager.getCache("whoAmI").put(user.getUserId(), updatedWhoAmI);

        log.info("Cache Warming: 사용자 id:{}의 whoAmI 캐시를 최신 데이터로 갱신했습니다 (DB 조회 방지).", u.getUserId());
        return user;
    }

    private WhoAmIDto getWhoAmILogic(User user) {
        User foundUser = userRepository.findById(user.getUserId())
                .orElseThrow(EntityNotFoundException::new);
        List<UserTopicDto> userTopic = userTopicService.getUserTopicsFromUser(foundUser);
        return WhoAmIDto.from(foundUser, userTopic);
    }

    public WhoAmIDto whoAmINoCache(User user) {
        log.info("[No-Cache] 강제 DB 조회 요청 - user: {}", user.getUserId());
        return getWhoAmILogic(user);
    }
}

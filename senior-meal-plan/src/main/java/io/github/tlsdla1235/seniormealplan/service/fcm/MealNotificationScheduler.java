package io.github.tlsdla1235.seniormealplan.service.fcm;

import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.enumPackage.MealType;
import io.github.tlsdla1235.seniormealplan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class MealNotificationScheduler {

    private final UserRepository userRepository;
    private final FcmService fcmService;

    // 매일 오후 2시 0분 0초에 실행 (Cron 표현식)
    // 테스트할 땐 "0 * * * * *" (매분)으로 바꿔서 해보세요.
//    @Scheduled(cron = "0 0 14 * * *")
    @Scheduled(cron = "0 * * * * *")
    @Transactional(readOnly = true)
    public void sendLunchReminder() {
        log.info("[스케줄러 시작] 점심 식사 미입력자 조회 중...");

        LocalDate today = LocalDate.now();

        // 1. 쿼리로 대상 토큰만 쏙 뽑아옴 (성능 최적화)
        List<String> targetTokens = userRepository.findTokensByNoMealLog(today, MealType.LUNCH);

        if (targetTokens.isEmpty()) {
            log.info("모든 유저가 식사를 기록했습니다. 알림 발송 없음.");
            return;
        }

        log.info("발송 대상: {}명", targetTokens.size());

        // 2. 반복문으로 발송 (유저가 많으면 Batch로 묶어 보내기 권장하지만, 지금은 이걸로 충분)
        for (String token : targetTokens) {
            fcmService.sendMessage(
                    token,
                    "점심 식사 하셨나요?",
                    "건강을 위해 식사 기록을 남겨주세요! 🍚"
            );
        }

        log.info("[스케줄러 종료] 알림 발송 완료");
    }
}
package io.github.tlsdla1235.seniormealplan.service.admin;


import io.github.tlsdla1235.seniormealplan.domain.preference.HealthTopic;
import io.github.tlsdla1235.seniormealplan.dto.tempdto.HealthTopicCreateRequestDto;
import io.github.tlsdla1235.seniormealplan.repository.HealthTopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성
public class AdminService {
    private final HealthTopicRepository healthTopicRepository;

    @Transactional
    public void createHealthTopic(HealthTopicCreateRequestDto requestDto) {
        HealthTopic healthTopic = new HealthTopic();
        healthTopic.setName(requestDto.getName());
        healthTopic.setDescription(requestDto.getDescription());
        healthTopic.setTopicType(requestDto.getTopicType());
        healthTopicRepository.save(healthTopic);
    }
}

package io.github.tlsdla1235.seniormealplan.domain.preference;

import io.github.tlsdla1235.seniormealplan.domain.enumPackage.TopicType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 알러지, 건강 목표, 질병 이력 등 건강 관련 주제를 통합 관리하는 마스터 엔티티.
 */
@Entity
@Table(name = "health_topics")
@Getter
@Setter
@NoArgsConstructor
public class HealthTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "topic_id")
    private Long topicId;

    @Enumerated(EnumType.STRING)
    @Column(name = "topic_type", nullable = false)
    private TopicType topicType;

    @Column(name = "name", nullable = false)
    private String name;

    @Lob
    @Column(name = "description")
    private String description;

    public HealthTopic(TopicType topicType, String name, String description) {
        this.topicType = topicType;
        this.name = name;
        this.description = description;
    }
}
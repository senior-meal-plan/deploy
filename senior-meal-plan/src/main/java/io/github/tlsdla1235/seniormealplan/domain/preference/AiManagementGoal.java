package io.github.tlsdla1235.seniormealplan.domain.preference;

import io.github.tlsdla1235.seniormealplan.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AI가 사용자의 입력을 분석하여 최종적으로 관리할 건강 목표를 설정하는 엔티티.
 */
@Entity
@Table(name = "ai_management_goals")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiManagementGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_topic_id", nullable = false)
    private HealthTopic healthTopic;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
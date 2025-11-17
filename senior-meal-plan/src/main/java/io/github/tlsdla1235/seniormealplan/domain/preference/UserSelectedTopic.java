package io.github.tlsdla1235.seniormealplan.domain.preference;

import io.github.tlsdla1235.seniormealplan.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 사용자가 직접 선택한 건강 토픽(알러지, 목표, 질병)을 연결하는 엔티티.
 */
@Entity
@Table(name = "user_selected_topics")
@Getter
@NoArgsConstructor
public class UserSelectedTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private HealthTopic healthTopic;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserSelectedTopic(User user, HealthTopic healthTopic) {
        this.user = user;
        this.healthTopic = healthTopic;
    }

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
package io.github.tlsdla1235.seniormealplan.domain.preference.legacy;

import io.github.tlsdla1235.seniormealplan.domain.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED) // 1. JOINED 상속 전략 사용
@DiscriminatorColumn(name = "pref_type") // 2. 어떤 자식인지 구분할 컬럼 지정
public abstract class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pref_id")
    private Integer prefId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

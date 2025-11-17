package io.github.tlsdla1235.seniormealplan.repository;

import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.preference.AiManagementGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AiManagementGoalRepository extends JpaRepository<AiManagementGoal, Long> {
    @Query("SELECT amg FROM AiManagementGoal amg JOIN FETCH amg.healthTopic WHERE amg.user = :user")
    List<AiManagementGoal> findAllByUserWithHealthTopic(@Param("user") User user);


    @Modifying
    @Query("DELETE FROM AiManagementGoal amg WHERE amg.user = :user")
    void deleteAllByUser(@Param("user") User user);
}

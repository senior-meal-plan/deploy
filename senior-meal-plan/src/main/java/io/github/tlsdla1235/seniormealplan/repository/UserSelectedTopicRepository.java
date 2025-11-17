package io.github.tlsdla1235.seniormealplan.repository;

import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.preference.UserSelectedTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserSelectedTopicRepository extends JpaRepository<UserSelectedTopic, Long> {
    @Query("SELECT ust FROM UserSelectedTopic ust JOIN FETCH ust.healthTopic WHERE ust.user = :user")
    List<UserSelectedTopic> findAllByUserWithHealthTopic(@Param("user") User user);

    @Modifying
    @Query("DELETE FROM UserSelectedTopic ust WHERE ust.user = :user")
    Integer deleteByUser(@Param("user") User user);
}

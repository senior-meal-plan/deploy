package io.github.tlsdla1235.seniormealplan.repository;

import io.github.tlsdla1235.seniormealplan.domain.User;
import io.github.tlsdla1235.seniormealplan.domain.report.WeeklyReport;
import io.github.tlsdla1235.seniormealplan.dto.weeklyreport.SimpleWeeklyReportDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long> {

    @Query("SELECT new io.github.tlsdla1235.seniormealplan.dto.weeklyreport.SimpleWeeklyReportDto(wr.reportId, wr.weekStart, wr.weekEnd) " +
            "FROM WeeklyReport wr " +
            "WHERE wr.user = :user " +
            "ORDER BY wr.weekEnd DESC")
    List<SimpleWeeklyReportDto> findSimpleReportsByUserOrderByWeekEndDesc(@Param("user") User user);
}

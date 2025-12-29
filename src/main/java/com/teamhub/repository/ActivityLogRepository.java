package com.teamhub.repository;

import com.teamhub.domain.activity.ActivityLog;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    @Query("SELECT a FROM ActivityLog a JOIN FETCH a.actor WHERE a.workspace.id = :workspaceId ORDER BY a.createdAt DESC")
    List<ActivityLog> findByWorkspaceIdOrderByCreatedAtDesc(@Param("workspaceId")Long workspaceId, Pageable pageable);

    @Query("SELECT a FROM ActivityLog a JOIN FETCH a.actor WHERE a.workspace.id = :workspaceId ORDER BY a.createdAt DESC")
    Page<ActivityLog> findPageByWorkspaceId(@Param("workspaceId") Long workspaceId, Pageable pageable);

    // DAU 계산 (네이티브 쿼리)
    @Query(value = "SELECT DATE(created_at) as activity_date, COUNT(DISTINCT actor_id) as user_count "
                 + "FROM activity_logs WHERE created_at >= :startDate "
                 + "GROUP BY DATE(created_at) ORDER BY activity_date DESC",
          nativeQuery = true)
    List<Object[]> countDailyActiveUsers(@Param("startDate")LocalDateTime startDate);
}

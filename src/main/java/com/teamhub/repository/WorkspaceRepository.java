package com.teamhub.repository;

import com.teamhub.domain.workspace.Workspace;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    @Query("SELECT w FROM Workspace w JOIN w.members m WHERE m.user.id = :userId")
    List<Workspace> findAllByUserId(@Param("userId") Long userId);

    // 특정 시간 이후 생성된 워크스페이스 수
    @Query("SELECT COUNT(w) FROM Workspace w WHERE w.createdAt > :dateTime")
    Long countByCreatedAtAfter(@Param("dateTime") LocalDateTime dateTime);

    // 일별 워크스페이스 생성 (네이티브 쿼리)
    @Query(value = "SELECT DATE(created_at) as created_date, COUNT(*) as create_count "
                 + "FROM workspace WHERE created_at >= :startDate "
                 + "GROUP BY DATE(created_at) ORDER BY create_date DESC",
           nativeQuery = true)
    List<Object[]> countDailyWorkspaces(@Param("startDate") LocalDateTime startDate);

    // 멤버 수 기준 TOP 워크스페이스
    @Query("SELECT w, COUNT(m) as memberCount FROM Workspace w "
        + "LEFT JOIN w.members m GROUP BY w ORDER BY memberCount DESC")
    List<Object[]> findTopWorkspacesByMemberCount(Pageable pageable);
}

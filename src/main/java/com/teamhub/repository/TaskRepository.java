package com.teamhub.repository;

import com.teamhub.domain.project.Task;
import com.teamhub.enums.project.TaskPriority;
import com.teamhub.enums.project.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByProjectIdOrderByDisplayOrderAsc(Long projectId);

    List<Task> findAllByProjectIdAndStatusOrderByDisplayOrderAsc(Long projectId, TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.assignee.id = :userId ORDER BY t.dueDate ASC")
    List<Task> findAllByAssigneeId(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId")
    Long countByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
    Long countByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") TaskStatus status);

    // 워크스페이스 내 검색
    @Query("SELECT t FROM Task t " +
            "WHERE t.project.workspace.id = :workspaceId " +
            "AND (:keyword IS NULL OR t.title LIKE %:keyword% OR t.description LIKE %:keyword%) " +
            "AND (:status IS NULL OR t.status = :status) " +
            "AND (:priority IS NULL OR t.priority = :priority) " +
            "AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId) " +
            "AND (:projectId IS NULL OR t.project.id = :projectId) " +
            "AND (:dueDateFrom IS NULL OR t.dueDate >= :dueDateFrom) " +
            "AND (:dueDateTo IS NULL OR t.dueDate <= :dueDateTo) " +
            "ORDER BY t.createdAt DESC")
    List<Task> searchTasks(
            @Param("workspaceId") Long workspaceId,
            @Param("keyword") String keyword,
            @Param("status") TaskStatus status,
            @Param("priority")TaskPriority priority,
            @Param("assigneeId") Long assigneeId,
            @Param("projectId") Long projectId,
            @Param("dueDateFrom") LocalDate dueDateFrom,
            @Param("dueDateTo") LocalDate dueDateTo
    );

    // 워크스페이스 내 전체 태스크 수
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.workspace.id = :workspaceId")
    Long countByWorkspaceId(@Param("workspaceId") Long workspaceId);

    // 워크스페이스 내 상태별 태스크 수
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.workspace.id = :workspaceId AND t.status = :status")
    Long countByWorkspaceIdAndStatus(@Param("workspaceId") Long workspaceId, @Param("status") TaskStatus status);

    // 멤버별 배정된 태스크 수
    @Query("SELECT t.assignee.id, COUNT(t) FROM Task t " +
        "WHERE t.project.workspace.id = :workspaceId AND t.assignee IS NOT NULL " +
        "GROUP BY t.assignee.id")
    List<Object[]> countByWorkspaceIdGroupByAssignee(@Param("workspaceId") Long workspaceId);

    // 멤버별 완료된 태스크 수
    @Query("SELECT t.assignee.id, COUNT(t) FROM Task t " +
        "WHERE t.project.workspace.id = :workspaceId AND t.assignee IS NOT NULL AND t.status = 'DONE' " +
        "GROUP BY t.assignee.id")
    List<Object[]> countCompletedByWorkspaceIdGroupByAssignee(@Param("workspaceId") Long workspaceId);

    // 마감 임박 태스크 (7일 이내)
    @Query("SELECT t FROM Task t WHERE t.project.workspace.id = :workspaceId " +
        "AND t.status != 'DONE' AND t.dueDate IS NOT NULL " +
        "AND t.dueDate BETWEEN :today AND :endDate ORDER BY t.dueDate ASC")
    List<Task> findUpcomingTasks(@Param("workspaceId") Long workspaceId,
        @Param("today") java.time.LocalDate today,
        @Param("endDate") java.time.LocalDate endDate);

    // 전체 태스크 수 (관리자용)
    @Query("SELECT COUNT(t) FROM Task t")
    Long countAll();

    //Task 목록 페이징
    Page<Task> findAllByProjectId(Long projectId, Pageable pageable);

    // 마감일 범위 내 태스크 조회 (스케줄러용)
    @Query("SELECT t FROM Task t " +
            "WHERE t.dueDate BETWEEN :startDate AND :endDate " +
            "AND t.status != 'DONE' " +
            "AND t.assignee IS NOT NULL")
    List<Task> findTasksDueBetween(@Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    // 삭제된 태스크 조회 (Soft Delete 우회)
    @Query(value = "SELECT * FROM tasks t " +
            "JOIN projects p ON t.project_id = p.id " +
            "WHERE p.workspace_id = :workspaceId AND t.is_deleted = true " +
            "ORDER BY t.deleted_at DESC", nativeQuery = true)
    List<Task> findDeletedByWorkspaceId(@Param("workspaceId") Long workspaceId);

    // 삭제된 태스크 포함 조회
    @Query(value = "SELECT * FROM tasks WHERE id = :taskId", nativeQuery = true)
    Optional<Task> findByIdIncludeDeleted(@Param("taskId") Long taskId);

    // 30일 이상 지난 삭제 항목 영구 삭제
    @Modifying
    @Query(value = "DELETE FROM tasks t USING projects p " +
            "WHERE t.project_id = p.id AND p.workspace_id = :workspaceId " +
            "AND t.is_deleted = true AND t.deleted_at < NOW() - INTERVAL 30 DAY", nativeQuery = true)
    int deleteOldDeletedItems(@Param("workspaceId") Long workspaceId);
}

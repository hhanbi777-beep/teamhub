package com.teamhub.repository;

import com.teamhub.domain.project.Task;
import com.teamhub.enums.project.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByProjectIdOrderByDisplayOrderAsc(Long projectId);

    List<Task> findAllByProjectIdAndStatusOrderByDisplayOrderAsc(Long projectId, TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.assignee.id = :userId ORDER BY t.dueDate ASC")
    List<Task> findAllByAssigneeId(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
    Long countByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") TaskStatus status);
}

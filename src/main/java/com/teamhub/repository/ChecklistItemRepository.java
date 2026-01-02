package com.teamhub.repository;

import com.teamhub.domain.project.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {
    List<ChecklistItem> findAllByTaskIdOrderByDisplayOrderAsc(Long taskId);

    @Query("SELECT COUNT(c) FROM ChecklistItem c WHERE c.task.id = :taskId")
    Long countByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT COUNT(c) FROM ChecklistItem c WHERE c.task.id = :taskId AND c.isCompleted = true")
    Long countCompletedByTaskId(@Param("taskId") Long taskId);
}
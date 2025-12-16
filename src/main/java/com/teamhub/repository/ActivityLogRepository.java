package com.teamhub.repository;

import com.teamhub.domain.activity.ActivityLog;
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

}

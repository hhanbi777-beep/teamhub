package com.teamhub.repository;

import com.teamhub.domain.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByWorkspaceId(Long workspaceId);

    @Query("SELECT p FROM Project p " +
            "WHERE p.workspace.id = :workspaceId " +
            "AND (:keyword IS NOT NULL OR p.name LIKE %:keyword% OR p.description LIKE %:keyword%) " +
            "ORDER BY p.createdAt DESC")
    List<Project> searchProjects(
            @Param("workspaceId") Long workspaceId,
            @Param("keyword") String keyword
    );

    Long countByWorkspaceId(Long workspaceId);

    // 삭제된 프로젝트 조회
    @Query(value = "SELECT * FROM projects WHERE workspace_id = :workspaceId AND is_deleted = true ORDER BY deleted_at DESC", nativeQuery = true)
    List<Project> findDeletedByWorkspaceId(@Param("workspaceId") Long workspaceId);

    // 삭제된 프로젝트 포함 조회
    @Query(value = "SELECT * FROM projects WHERE id = :projectId", nativeQuery = true)
    Optional<Project> findByIdIncludeDeleted(@Param("projectId") Long projectId);

    // 30일 이상 지난 삭제 항목 영구 삭제
    @Modifying
    @Query(value = "DELETE FROM projects WHERE workspace_id = :workspaceId AND is_deleted = true AND deleted_at < NOW() - INTERVAL 30 DAY", nativeQuery = true)
    int deleteOldDeletedItems(@Param("workspaceId") Long workspaceId);
}

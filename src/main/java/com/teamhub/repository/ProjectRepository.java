package com.teamhub.repository;

import com.teamhub.domain.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
}

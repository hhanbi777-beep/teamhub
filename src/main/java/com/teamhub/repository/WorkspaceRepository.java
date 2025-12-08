package com.teamhub.repository;

import com.teamhub.domain.workspace.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    @Query("SELECT w FROM Workspace w JOIN w.member m WHERE m.user.id = :userId")
    List<Workspace> findAllByUserId(@Param("userId") Long userId);
}

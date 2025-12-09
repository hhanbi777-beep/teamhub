package com.teamhub.repository;

import com.teamhub.domain.workspace.WorkspaceMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, Long> {

    @Query("SELECT wm FROM WorkspaceMember wm WHERE wm.workspace.id = :workspacedId AND wm.user.id = :userId")
    Optional<WorkspaceMember> findByWorkspaceIdAndUserId(@Param("workspaceId") Long workspaceId,
                                                         @Param("userId")Long userId);

    @Query("SELECT wm FROM WorkspaceMember wm JOIN FETCH wm.user WHERE wm.workspace.id = :workspaceId")
    List<WorkspaceMember> findAllByWorkspaceId(@Param("workspaceId") Long workspaceId);

    boolean existsByWorkspaceIdAndUserId(Long workspaceId, Long userId);
}

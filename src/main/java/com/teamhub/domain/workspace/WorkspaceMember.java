package com.teamhub.domain.workspace;

import com.teamhub.domain.common.BaseEntity;
import com.teamhub.domain.user.User;
import com.teamhub.enums.workspace.WorkspaceRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "workspace_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"workspace_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WorkspaceMember  extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkspaceRole role;

    public void changeRole(WorkspaceRole role) {
        this.role = role;
    }

    public boolean canManageMembers() {
        return role == WorkspaceRole.OWNER || role == WorkspaceRole.ADMIN;
    }

    public boolean canManageProjects() {
        return role == WorkspaceRole.OWNER || role == WorkspaceRole.ADMIN;
    }

    public boolean canEditTasks() {
        return role != WorkspaceRole.VIEWER;
    }

    public boolean isOwner() {
        return role == WorkspaceRole.OWNER;
    }
}

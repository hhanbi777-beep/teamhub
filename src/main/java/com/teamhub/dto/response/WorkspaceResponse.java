package com.teamhub.dto.response;

import com.teamhub.domain.workspace.Workspace;
import com.teamhub.enums.workspace.WorkspaceRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class WorkspaceResponse {

    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private String ownerName;
    private WorkspaceRole myRole;
    private LocalDateTime createdAt;

    public static WorkspaceResponse of(Workspace workspace, WorkspaceRole myRole) {
        return WorkspaceResponse.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .ownerId(workspace.getOwner().getId())
                .ownerName(workspace.getOwner().getName())
                .myRole(myRole)
                .createdAt(workspace.getCreatedAt())
                .build();
    }
}

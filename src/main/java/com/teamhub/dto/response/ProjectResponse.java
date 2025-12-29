package com.teamhub.dto.response;

import com.teamhub.domain.project.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private Long workspaceId;
    private Long taskCount;
    private LocalDateTime createdAt;

    public static ProjectResponse of(Project project, Long taskCount) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .workspaceId(project.getWorkspace().getId())
                .taskCount(taskCount)
                .createdAt(project.getCreatedAt())
                .build();
    }
}

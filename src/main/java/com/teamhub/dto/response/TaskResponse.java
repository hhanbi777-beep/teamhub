package com.teamhub.dto.response;

import com.teamhub.domain.project.Task;
import com.teamhub.enums.project.TaskPriority;
import com.teamhub.enums.project.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private Long projectId;
    private AssigneeInfo assignee;
    private LocalDate dueDate;
    private Integer displayOrder;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AssigneeInfo {
        private Long id;
        private String name;
        private String profileImage;
    }

    public static TaskResponse of(Task task){
        AssigneeInfo assigneeInfo = null;
        if (task.getAssignee() != null) {
            assigneeInfo = AssigneeInfo.builder()
                    .id(task.getAssignee().getId())
                    .name(task.getAssignee().getName())
                    .profileImage(task.getAssignee().getProfileImage())
                    .build();
        }

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .projectId(task.getProject().getId())
                .assignee(assigneeInfo)
                .dueDate(task.getDueDate())
                .displayOrder(task.getDisplayOrder())
                .createdAt(task.getCreatedAt())
                .build();
    }

}

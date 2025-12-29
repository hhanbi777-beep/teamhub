package com.teamhub.dto.request;

import com.teamhub.enums.project.TaskPriority;
import com.teamhub.enums.project.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class TaskRequest {

    @NotBlank(message = "테스크 제목을 필수입니다")
    private String title;

    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private Long assigneeId;

    private LocalDate dueDate;
}

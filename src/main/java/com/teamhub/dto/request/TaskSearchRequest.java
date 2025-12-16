package com.teamhub.dto.request;

import com.teamhub.enums.project.TaskPriority;
import com.teamhub.enums.project.TaskStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class TaskSearchRequest {

    private String keyword;           // 제목, 설명 검색
    private TaskStatus status;        // 상태 필터
    private TaskPriority priority;    // 우선순위 필터
    private Long assigneeId;          // 담당자 필터
    private LocalDate dueDateFrom;    // 마감일 시작
    private LocalDate dueDateTo;      // 마감일 종료
    private Long projectId;           // 프로젝트 필터

}

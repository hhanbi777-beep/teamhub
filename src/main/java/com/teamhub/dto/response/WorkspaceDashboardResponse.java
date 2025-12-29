package com.teamhub.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class WorkspaceDashboardResponse {

    // 테스크 현황
    private TaskSummary taskSummary;

    // 프로젝트별 진행률
    private List<ProjectProgress> projectProgresses;

    // 멤버별 태스크 현홍
    private List<MemberTaskSummary> memberTaskSummaries;

    // 최근 활동
    private List<ActivityLogResponse> recentActivities;

    // 마감 임박 태스크
    private List<TaskResponse> upcomingTasks;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class TaskSummary {
        private Long total;
        private Long todo;
        private Long inProgress;
        private Long review;
        private Long done;
        private Double completionRate;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ProjectProgress {
        private Long projectId;
        private String projectName;
        private Long totalTasks;
        private Long completedTasks;
        private Double progressRate;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class MemberTaskSummary {
        private Long userId;
        private String userName;
        private String profileImage;
        private Long assignedTasks;
        private Long completedTasks;
    }
}

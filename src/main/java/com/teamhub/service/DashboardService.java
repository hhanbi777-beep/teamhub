package com.teamhub.service;

import com.teamhub.domain.project.Project;
import com.teamhub.domain.workspace.WorkspaceMember;
import com.teamhub.dto.response.ActivityLogResponse;
import com.teamhub.dto.response.TaskResponse;
import com.teamhub.dto.response.WorkspaceDashboardResponse;
import com.teamhub.enums.ErrorCode;
import com.teamhub.enums.project.TaskStatus;
import com.teamhub.exception.CustomException;
import com.teamhub.repository.ActivityLogRepository;
import com.teamhub.repository.ProjectRepository;
import com.teamhub.repository.TaskRepository;
import com.teamhub.repository.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ActivityLogRepository activityLogRepository;
    private final TaskRepository taskRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ProjectRepository projectRepository;


    public WorkspaceDashboardResponse getWorkspaceDashboard(Long userId, Long workspaceId) {
        // 권한 확인
        findMemberOrThrow(workspaceId, userId);

        return WorkspaceDashboardResponse.builder()
                .taskSummary(getTaskSummary(workspaceId))
                .projectProgresses(getProjectProgresses(workspaceId))
                .memberTaskSummaries(getMemberTaskSummaries(workspaceId))
                .recentActivities(getRecentActivities(workspaceId))
                .upcomingTasks(getUpcomingTasks(workspaceId))
                .build();
    }

    private WorkspaceDashboardResponse.TaskSummary getTaskSummary(Long workspaceId) {
        Long total = taskRepository.countByWorkspaceId(workspaceId);
        Long todo = taskRepository.countByWorkspaceIdAndStatus(workspaceId, TaskStatus.TODO);
        Long inProgress = taskRepository.countByWorkspaceIdAndStatus(workspaceId, TaskStatus.IN_PROGRESS);
        Long review = taskRepository.countByWorkspaceIdAndStatus(workspaceId, TaskStatus.REVIEW);
        Long done = taskRepository.countByWorkspaceIdAndStatus(workspaceId, TaskStatus.DONE);

        Double completionRate = total > 0 ? (done * 100.0) / total : 0.0;

        return WorkspaceDashboardResponse.TaskSummary.builder()
                .total(total)
                .todo(todo)
                .inProgress(inProgress)
                .review(review)
                .done(done)
                .completionRate(Math.round(completionRate * 10) / 10.0)
                .build();
    }

    private List<WorkspaceDashboardResponse.ProjectProgress> getProjectProgresses(Long workspaceId) {
        List<Project> projects = projectRepository.findAllByWorkspaceId(workspaceId);

        return projects.stream().map(project -> {
            Long totalTasks = taskRepository.countByProjectId(project.getId());
            Long completedTasks = taskRepository.countByProjectIdAndStatus(project.getId(), TaskStatus.DONE);
            Double progressRate = totalTasks > 0 ? (completedTasks * 100.0) / totalTasks : 0.0;

            return WorkspaceDashboardResponse.ProjectProgress.builder()
                    .projectId(project.getId())
                    .projectName(project.getName())
                    .totalTasks(totalTasks)
                    .completedTasks(completedTasks)
                    .progressRate(Math.round(progressRate * 10) / 10.0)
                    .build();
        }).collect(Collectors.toList());
    }

    private List<WorkspaceDashboardResponse.MemberTaskSummary> getMemberTaskSummaries(Long workspaceId) {
        List<WorkspaceMember> members = workspaceMemberRepository.findAllByWorkspaceId(workspaceId);

        // 멤버별 배정된 태스크 수
        Map<Long, Long> assignedMap = taskRepository.countByWorkspaceIdGroupByAssignee(workspaceId)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));

        // 멤버별 완료된 태스크 수
        Map<Long, Long> completedMap = taskRepository.countCompletedByWorkspaceIdGroupByAssignee(workspaceId)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> (Long) arr[1]
                ));

        return members.stream().map(member -> {
            Long memberId = member.getUser().getId();
            return WorkspaceDashboardResponse.MemberTaskSummary.builder()
                    .userId(memberId)
                    .userName(member.getUser().getName())
                    .profileImage(member.getUser().getProfileImage())
                    .assignedTasks(assignedMap.getOrDefault(memberId, 0L))
                    .completedTasks(completedMap.getOrDefault(memberId, 0L))
                    .build();
        }).collect(Collectors.toList());
    }

    //helper methods
    private List<ActivityLogResponse> getRecentActivities(Long workspaceId) {
        return activityLogRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId, PageRequest.of(0, 10))
                .stream()
                .map(ActivityLogResponse::of)
                .collect(Collectors.toList());
    }

    private List<TaskResponse> getUpcomingTasks(Long workspaceId) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(7);

        return taskRepository.findUpcomingTasks(workspaceId, today, endDate)
                .stream()
                .map(TaskResponse::of)
                .collect(Collectors.toList());
    }

    private WorkspaceMember findMemberOrThrow(Long workspaceId, Long userId) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_ACCESS_DENIED));
    }

}

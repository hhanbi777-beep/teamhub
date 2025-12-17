package com.teamhub.service;

import com.teamhub.domain.workspace.WorkspaceMember;
import com.teamhub.dto.request.ProjectSearchRequest;
import com.teamhub.dto.request.TaskSearchRequest;
import com.teamhub.dto.response.ProjectResponse;
import com.teamhub.dto.response.TaskResponse;
import com.teamhub.enums.ErrorCode;
import com.teamhub.exception.CustomException;
import com.teamhub.repository.ProjectRepository;
import com.teamhub.repository.TaskRepository;
import com.teamhub.repository.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public List<TaskResponse> searchTasks(Long userId, Long workspaceId, TaskSearchRequest req) {

        findMemberOrThrow(userId, workspaceId);

        return taskRepository.searchTasks(
                workspaceId,
                req.getKeyword(),
                req.getStatus(),
                req.getPriority(),
                req.getAssigneeId(),
                req.getProjectId(),
                req.getDueDateFrom(),
                req.getDueDateTo()
        ).stream()
                .map(TaskResponse::of)
                .collect(Collectors.toList());
    }

    public List<ProjectResponse> searchProjects(Long userId, Long workspaceId, ProjectSearchRequest req) {

        findMemberOrThrow(userId, workspaceId);

        return projectRepository.searchProjects(workspaceId, req.getKeyword())
                .stream()
                .map(project -> ProjectResponse.of(project, taskRepository.countByProjectId(project.getId())))
                .collect(Collectors.toList());
    }

    private WorkspaceMember findMemberOrThrow(Long workspaceId, Long userId) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_ACCESS_DENIED));
    }
}

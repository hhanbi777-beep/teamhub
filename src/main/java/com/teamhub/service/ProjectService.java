package com.teamhub.service;


import com.teamhub.domain.project.Project;
import com.teamhub.domain.workspace.Workspace;
import com.teamhub.domain.workspace.WorkspaceMember;
import com.teamhub.dto.request.ProjectRequest;
import com.teamhub.dto.response.ProjectResponse;
import com.teamhub.enums.ErrorCode;
import com.teamhub.exception.CustomException;
import com.teamhub.repository.ProjectRepository;
import com.teamhub.repository.TaskRepository;
import com.teamhub.repository.WorkspaceMemberRepository;
import com.teamhub.repository.WorkspaceRepository;
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
public class ProjectService {

    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public ProjectResponse createProject(Long userId, Long workspaceId, ProjectRequest request) {
        WorkspaceMember member = findMemberOrThrow(workspaceId, userId);

        if(!member.canManageProjects()) {
            throw new CustomException(ErrorCode.PROJECT_CREATE_DENIED);
        }

        Workspace workspace = findWorkspaceById(workspaceId);

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .workspace(workspace)
                .build();

        projectRepository.save(project);

        log.info("Project created: {} in workspace: {}", project.getName(), workspaceId);

        return ProjectResponse.of(project, 0L);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjects(Long userId, Long workspaceId) {
        findMemberOrThrow(workspaceId, userId);

        return projectRepository.findAllByWorkspaceId(workspaceId)
                .stream()
                .map(project -> {
                    long taskCount = taskRepository.countByProjectIdAndStatus(project.getId(), null);
                    return ProjectResponse.of(project, (long) project.getTasks().size());
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long userId, Long projectId) {
        Project project = findProjectById(projectId);
        findMemberOrThrow(project.getWorkspace().getId(), userId);
        return ProjectResponse.of(project, (long) project.getTasks().size());
    }

    @Transactional(readOnly = true)
    public ProjectResponse updateProject(Long userId, Long projectId, ProjectRequest request) {
        Project project = findProjectById(projectId);
        WorkspaceMember member = findMemberOrThrow(project.getWorkspace().getId(),  userId);
        if (!member.canManageProjects()) {
            throw new CustomException(ErrorCode.PROJECT_UPDATE_DENIED);
        }

        project.updateInfo(request.getName(), request.getDescription());

        return ProjectResponse.of(project, (long)project.getTasks().size());
    }

    @Transactional
    public void deleteProject(Long userId, Long projectId) {
        Project project = findProjectById(projectId);
        WorkspaceMember member = findMemberOrThrow(project.getWorkspace().getId(),  userId);

        if(!member.canManageProjects()) {
            throw new CustomException(ErrorCode.PROJECT_DELETE_DENIED);
        }

        projectRepository.delete(project);

        log.info("Project deleted: {}", projectId);
    }

    //Helper methods
    private Workspace findWorkspaceById(Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));
    }

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
    }

    private WorkspaceMember findMemberOrThrow(Long workspaceId, Long userId) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_ACCESS_DENIED));
    }
}

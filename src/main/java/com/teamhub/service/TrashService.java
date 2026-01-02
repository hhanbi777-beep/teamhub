package com.teamhub.service;

import com.teamhub.domain.project.Project;
import com.teamhub.domain.project.Task;
import com.teamhub.domain.workspace.Workspace;
import com.teamhub.domain.workspace.WorkspaceMember;
import com.teamhub.dto.response.ProjectResponse;
import com.teamhub.dto.response.TaskResponse;
import com.teamhub.dto.response.WorkspaceResponse;
import com.teamhub.enums.ErrorCode;
import com.teamhub.exception.CustomException;
import com.teamhub.repository.ProjectRepository;
import com.teamhub.repository.TaskRepository;
import com.teamhub.repository.WorkspaceMemberRepository;
import com.teamhub.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrashService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    /**
     * 삭제된 태스크 목록 조회
     */
    public List<TaskResponse> getDeletedTasks(Long userId, Long workspaceId) {
        WorkspaceMember member = findMemberOrThrow(workspaceId, userId);

        if (!member.canManageProjects()) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        return taskRepository.findDeletedByWorkspaceId(workspaceId).stream()
                .map(TaskResponse::of)
                .collect(Collectors.toList());
    }

    /**
     * 삭제된 프로젝트 목록 조회
     */
    public List<ProjectResponse> getDeletedProjects(Long userId, Long workspaceId) {
        WorkspaceMember member = findMemberOrThrow(workspaceId, userId);

        if (!member.canManageProjects()) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        return projectRepository.findDeletedByWorkspaceId(workspaceId).stream()
                .map(project -> ProjectResponse.of(project, 0L))
                .collect(Collectors.toList());
    }

    /**
     * 태스크 복구
     */
    @Transactional
    public TaskResponse restoreTask(Long userId, Long taskId) {
        Task task = taskRepository.findByIdIncludeDeleted(taskId)
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));

        WorkspaceMember member = findMemberOrThrow(task.getProject().getWorkspace().getId(), userId);

        if (!member.canManageProjects()) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        task.restore();
        log.info("Task restored: {}", taskId);

        return TaskResponse.of(task);
    }

    /**
     * 프로젝트 복구
     */
    @Transactional
    public ProjectResponse restoreProject(Long userId, Long projectId) {
        Project project = projectRepository.findByIdIncludeDeleted(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        WorkspaceMember member = findMemberOrThrow(project.getWorkspace().getId(), userId);

        if (!member.canManageProjects()) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        project.restore();
        log.info("Project restored: {}", projectId);

        return ProjectResponse.of(project, 0L);
    }

    /**
     * 태스크 영구 삭제
     */
    @Transactional
    public void permanentDeleteTask(Long userId, Long taskId) {
        Task task = taskRepository.findByIdIncludeDeleted(taskId)
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));

        WorkspaceMember member = findMemberOrThrow(task.getProject().getWorkspace().getId(), userId);

        if (!member.isOwner()) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        taskRepository.delete(task);
        log.info("Task permanently deleted: {}", taskId);
    }

    /**
     * 프로젝트 영구 삭제
     */
    @Transactional
    public void permanentDeleteProject(Long userId, Long projectId) {
        Project project = projectRepository.findByIdIncludeDeleted(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        WorkspaceMember member = findMemberOrThrow(project.getWorkspace().getId(), userId);

        if (!member.isOwner()) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        projectRepository.delete(project);
        log.info("Project permanently deleted: {}", projectId);
    }

    /**
     * 휴지통 비우기 (30일 이상 지난 항목)
     */
    @Transactional
    public int emptyTrash(Long userId, Long workspaceId) {
        WorkspaceMember member = findMemberOrThrow(workspaceId, userId);

        if (!member.isOwner()) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        int deletedTasks = taskRepository.deleteOldDeletedItems(workspaceId);
        int deletedProjects = projectRepository.deleteOldDeletedItems(workspaceId);

        log.info("Trash emptied for workspace: {}. Tasks: {}, Projects: {}", workspaceId, deletedTasks, deletedProjects);

        return deletedTasks + deletedProjects;
    }

    private WorkspaceMember findMemberOrThrow(Long workspaceId, Long userId) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_ACCESS_DENIED));
    }
}
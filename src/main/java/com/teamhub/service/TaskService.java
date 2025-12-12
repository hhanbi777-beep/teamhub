package com.teamhub.service;

import com.teamhub.domain.project.Project;
import com.teamhub.domain.project.Task;
import com.teamhub.domain.user.User;
import com.teamhub.domain.workspace.WorkspaceMember;
import com.teamhub.dto.request.TaskRequest;
import com.teamhub.dto.response.TaskResponse;
import com.teamhub.enums.project.TaskStatus;
import com.teamhub.exception.CustomException;
import com.teamhub.repository.ProjectRepository;
import com.teamhub.repository.TaskRepository;
import com.teamhub.repository.UserRepository;
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
public class TaskService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    @Transactional
    public TaskResponse createTask(Long userId, Long projectId, TaskRequest request){
        Project project = findProjectById(projectId);
        WorkspaceMember member = findMemberOrThrow(project.getWorkspace().getId(), userId);

        if(!member.canEditTasks()) {
            throw new CustomException("테스크 생성 권한이 없습니다", HttpStatus.FORBIDDEN);
        }

        User creator = findUserById(userId);
        User assignee = null;

        if(request.getAssigneeId() != null) {
            assignee = findUserById(request.getAssigneeId());
            //담당자도 워크스페이스 멤버인지 확인
            findMemberOrThrow(project.getWorkspace().getId(), assignee.getId());
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .priority(request.getPriority())
                .project(project)
                .assignee(assignee)
                .createdBy(creator)
                .dueDate(request.getDueDate())
                .build();

        taskRepository.save(task);

        log.info("Task created: {} in project: {}", task.getTitle(), projectId);

        return TaskResponse.of(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasks(Long userId, Long projectId){
        Project project = findProjectById(projectId);
        findMemberOrThrow(project.getWorkspace().getId(), userId);

        return taskRepository.findAllByProjectIdOrderByDisplayOrderAsc(projectId)
                .stream()
                .map(TaskResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByStatus(Long userId, Long projectId, TaskStatus status){
        Project project = findProjectById(projectId);
        findMemberOrThrow(project.getWorkspace().getId(), userId);

        return taskRepository.findAllByProjectIdAndStatusOrderByDisplayOrderAsc(projectId, status)
                .stream()
                .map(TaskResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getMyTasks(Long userId) {
        return taskRepository.findAllByAssigneeId(userId)
                .stream()
                .map(TaskResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long userId, Long taskId){
        Task task = findTaskById(taskId);
        findMemberOrThrow(task.getProject().getWorkspace().getId(), userId);

        return TaskResponse.of(task);
    }

    @Transactional
    public TaskResponse updateTask(Long userId, Long taskId, TaskRequest request){
        Task task = findTaskById(taskId);
        WorkspaceMember member = findMemberOrThrow(task.getProject().getWorkspace().getId(), userId);

        if(!member.canEditTasks()) {
            throw new CustomException("테스크 수정 권한이 없습니다", HttpStatus.FORBIDDEN);
        }

        task.updateInfo(request.getTitle(), request.getDescription(), request.getPriority(), request.getDueDate());

        if(request.getAssigneeId() != null) {
            User assignee = findUserById(request.getAssigneeId());
            findMemberOrThrow(task.getProject().getWorkspace().getId(), assignee.getId());
            task.assignTo(assignee);
        }

        return TaskResponse.of(task);
    }

    @Transactional
    public TaskResponse changeTaskStatus(Long userId, Long taskId, TaskStatus status){
        Task task = findTaskById(taskId);
        WorkspaceMember member = findMemberOrThrow(task.getProject().getWorkspace().getId(), userId);

        if(!member.canEditTasks()) {
            throw new CustomException("테스크 상태 변경 권한이 없습니다", HttpStatus.FORBIDDEN);
        }

        task.changeStatus(status);

        log.info("Task change status:{} -> {}", taskId, status);

        return TaskResponse.of(task);
    }

    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        Task task = findTaskById(taskId);
        WorkspaceMember member = findMemberOrThrow(task.getProject().getWorkspace().getId(), userId);

        if(!member.canEditTasks()) {
            throw new CustomException("테스크 삭제 권한이 없습니다", HttpStatus.FORBIDDEN);
        }

        taskRepository.delete(task);
        log.info("Task deleted: {}", taskId);
    }

    //Helper methods
    private User findUserById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));
    }

    private Project findProjectById(Long projectId){
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException("프로젝트를 찾을 수 없습니다", HttpStatus.NOT_FOUND));
    }

    private Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException("테스크를 찾을 수 없습니다", HttpStatus.NOT_FOUND));
    }

    private WorkspaceMember findMemberOrThrow(Long workspaceId, Long userId) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new CustomException("", HttpStatus.FORBIDDEN));
    }
}

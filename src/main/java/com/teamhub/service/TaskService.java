package com.teamhub.service;

import com.teamhub.domain.project.Project;
import com.teamhub.domain.project.Task;
import com.teamhub.domain.user.User;
import com.teamhub.domain.workspace.WorkspaceMember;
import com.teamhub.dto.request.TaskRequest;
import com.teamhub.dto.response.TaskResponse;
import com.teamhub.enums.ErrorCode;
import com.teamhub.enums.activity.ActivityType;
import com.teamhub.enums.activity.TargetType;
import com.teamhub.enums.project.TaskStatus;
import com.teamhub.exception.CustomException;
import com.teamhub.repository.ProjectRepository;
import com.teamhub.repository.TaskRepository;
import com.teamhub.repository.UserRepository;
import com.teamhub.repository.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    @Transactional
    public TaskResponse createTask(Long userId, Long projectId, TaskRequest request){
        Project project = findProjectById(projectId);
        WorkspaceMember member = findMemberOrThrow(project.getWorkspace().getId(), userId);

        if(!member.canEditTasks()) {
            throw new CustomException(ErrorCode.TASK_CREATE_DENIED);
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

        //활동로그
        activityLogService.log(
                project.getWorkspace(),
                creator,
                ActivityType.TASK_CREATED,
                TargetType.TASK,
                task.getId(),
                task.getTitle(),
                null
        );

        //담당자에게 알림(본인제외)
        if (assignee != null && !assignee.getId().equals(userId)) {
            notificationService.sendTaskAssignedNotification(task, creator);
        }

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
    public TaskResponse updateTask(Long userId, Long taskId, TaskRequest req){
        Task task = findTaskById(taskId);
        WorkspaceMember member = findMemberOrThrow(task.getProject().getWorkspace().getId(), userId);

        if(!member.canEditTasks()) {
            throw new CustomException(ErrorCode.TASK_UPDATE_DENIED);
        }

        User updater = findUserById(userId);
        User previousAssignee = task.getAssignee();

        task.updateInfo(req.getTitle(), req.getDescription(), req.getPriority(), req.getDueDate());

        if(req.getStatus() != null) {
            String oldStatus = task.getStatus().name();
            task.changeStatus(req.getStatus());

            // 상태 변경 알림
            notificationService.sendTaskStatusChangedNotification(task, updater, oldStatus, req.getStatus().name());
        }


        if(req.getAssigneeId() != null) {
            User newAssignee = findUserById(req.getAssigneeId());
            findMemberOrThrow(task.getProject().getWorkspace().getId(), newAssignee.getId());
            task.assignTo(newAssignee);

            //새 담당자에게 알림(이전 담당자와 다르고, 본인이 아닐 떄)
            boolean isDifferentAssignee = previousAssignee == null || !previousAssignee.getId().equals(newAssignee.getId());
            boolean isNotSelf = !newAssignee.getId().equals(userId);

            if(isDifferentAssignee && isNotSelf) {
                notificationService.sendTaskAssignedNotification(task, updater);
            }
        }

        // 활동로그
        activityLogService.log(
                task.getProject().getWorkspace(),
                updater,
                ActivityType.TASK_CREATED,
                TargetType.TASK,
                task.getId(),
                task.getTitle(),
                null
        );

        return TaskResponse.of(task);
    }

    @Transactional
    public TaskResponse changeTaskStatus(Long userId, Long taskId, TaskStatus status){
        Task task = findTaskById(taskId);
        WorkspaceMember member = findMemberOrThrow(task.getProject().getWorkspace().getId(), userId);

        if(!member.canEditTasks()) {
            throw new CustomException(ErrorCode.TASK_STATUS_CHANGE_DENIED);
        }

        User changer = findUserById(userId);
        String oldStatus = task.getStatus().name();

        task.changeStatus(status);

        //활동로그
        activityLogService.log(
                task.getProject().getWorkspace(),
                changer,
                ActivityType.TASK_STATUS_CHANGED,
                TargetType.TASK,
                task.getId(),
                task.getTitle(),
                oldStatus + " → " + status.name()
        );
        
        //상태변셩 알림
        notificationService.sendTaskStatusChangedNotification(task, changer, oldStatus, status.name());

        log.info("Task change status:{} -> {}", taskId, status);

        return TaskResponse.of(task);
    }

    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        Task task = findTaskById(taskId);
        WorkspaceMember member = findMemberOrThrow(task.getProject().getWorkspace().getId(), userId);

        if(!member.canEditTasks()) {
            throw new CustomException(ErrorCode.TASK_DELETE_DENIED);
        }
        
        User deleter = findUserById(userId);
        
        //활동로그
        activityLogService.log(
                task.getProject().getWorkspace(),
                deleter,
                ActivityType.TASK_DELETED,
                TargetType.TASK,
                task.getId(),
                task.getTitle(),
                null
        );

        task.delete();
        log.info("Task soft deleted: {}", taskId);
    }

    //Helper methods
    private User findUserById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Project findProjectById(Long projectId){
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));
    }

    private Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));
    }

    private WorkspaceMember findMemberOrThrow(Long workspaceId, Long userId) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_ACCESS_DENIED));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasksPaged(Long userId, Long projectId, Pageable pageable) {
        Project project = findProjectById(projectId);
        findMemberOrThrow(project.getWorkspace().getId(), userId);

        return taskRepository.findAllByProjectId(projectId, pageable)
                .map(TaskResponse::of);
    }
}

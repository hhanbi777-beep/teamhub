package com.teamhub.service;

import com.teamhub.domain.project.Label;
import com.teamhub.domain.project.Task;
import com.teamhub.domain.workspace.Workspace;
import com.teamhub.domain.workspace.WorkspaceMember;
import com.teamhub.dto.request.LabelRequest;
import com.teamhub.dto.response.LabelResponse;
import com.teamhub.enums.ErrorCode;
import com.teamhub.exception.CustomException;
import com.teamhub.repository.LabelRepository;
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
public class LabelService {

    private final LabelRepository labelRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public LabelResponse createLabel(Long userId, Long workspaceId, LabelRequest request) {
        WorkspaceMember member = findMemberOrThrow(workspaceId, userId);

        if (!member.canManageProjects()) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 중복 이름 체크
        labelRepository.findByWorkspaceIdAndName(workspaceId, request.getName())
                .ifPresent(l -> {
                    throw new CustomException(ErrorCode.DUPLICATE_LABEL_NAME);
                });

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));

        Label label = Label.builder()
                .name(request.getName())
                .color(request.getColor())
                .workspace(workspace)
                .build();

        return LabelResponse.of(labelRepository.save(label));
    }

    public List<LabelResponse> getLabels(Long userId, Long workspaceId) {
        findMemberOrThrow(workspaceId, userId);

        return labelRepository.findAllByWorkspaceId(workspaceId).stream()
                .map(LabelResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public LabelResponse updateLabel(Long userId, Long labelId, LabelRequest request) {
        Label label = findLabelById(labelId);
        WorkspaceMember member = findMemberOrThrow(label.getWorkspace().getId(), userId);

        if (!member.canManageProjects()) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        label.update(request.getName(), request.getColor());
        return LabelResponse.of(label);
    }

    @Transactional
    public void deleteLabel(Long userId, Long labelId) {
        Label label = findLabelById(labelId);
        WorkspaceMember member = findMemberOrThrow(label.getWorkspace().getId(), userId);

        if (!member.canManageProjects()) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        labelRepository.delete(label);
    }

    @Transactional
    public void addLabelToTask(Long userId, Long taskId, Long labelId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));
        Label label = findLabelById(labelId);

        findMemberOrThrow(task.getProject().getWorkspace().getId(), userId);

        task.addLabel(label);
    }

    @Transactional
    public void removeLabelFromTask(Long userId, Long taskId, Long labelId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));
        Label label = findLabelById(labelId);

        findMemberOrThrow(task.getProject().getWorkspace().getId(), userId);

        task.removeLabel(label);
    }

    //helper methods
    private Label findLabelById(Long labelId) {
        return labelRepository.findById(labelId)
                .orElseThrow(() -> new CustomException(ErrorCode.LABEL_NOT_FOUND));
    }

    private WorkspaceMember findMemberOrThrow(Long workspaceId, Long userId) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_ACCESS_DENIED));
    }
}

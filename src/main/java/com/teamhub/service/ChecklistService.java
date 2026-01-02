package com.teamhub.service;

import com.teamhub.domain.project.ChecklistItem;
import com.teamhub.domain.project.Task;
import com.teamhub.domain.workspace.WorkspaceMember;
import com.teamhub.dto.request.ChecklistItemRequest;
import com.teamhub.dto.response.ChecklistItemResponse;
import com.teamhub.enums.ErrorCode;
import com.teamhub.exception.CustomException;
import com.teamhub.repository.ChecklistItemRepository;
import com.teamhub.repository.TaskRepository;
import com.teamhub.repository.WorkspaceMemberRepository;
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
public class ChecklistService {

    private final TaskRepository taskRepository;
    private final ChecklistItemRepository checklistItemRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    @Transactional
    public ChecklistItemResponse addItem(Long userId, Long taskId, ChecklistItemRequest req) {
        Task task = findTaskById(taskId);
        findMemberOrThrow(task.getProject().getWorkspace().getId(), userId);

        int nextOrder = checklistItemRepository.countByTaskId(taskId).intValue();

        ChecklistItem item = ChecklistItem.builder()
                .content(req.getContent())
                .task(task)
                .displayOrder(nextOrder)
                .build();

        return ChecklistItemResponse.of(checklistItemRepository.save(item));
    }

    public List<ChecklistItemResponse> getItems(Long userId, Long taskId) {
        Task task = findTaskById(taskId);
        findMemberOrThrow(task.getProject().getWorkspace().getId(), userId);

        return checklistItemRepository.findAllByTaskIdOrderByDisplayOrderAsc(taskId).stream()
                .map(ChecklistItemResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public ChecklistItemResponse updateItem(Long userId, Long itemId, ChecklistItemRequest req) {
        ChecklistItem item = findItemById(itemId);
        findMemberOrThrow(item.getTask().getProject().getWorkspace().getId(), userId);

        item.update(req.getContent());
        return ChecklistItemResponse.of(item);
    }

    @Transactional
    public ChecklistItemResponse toggleItem(Long userId, Long itemId) {
        ChecklistItem item = findItemById(itemId);
        findMemberOrThrow(item.getTask().getProject().getWorkspace().getId(), userId);

        item.toggleComplete();
        return ChecklistItemResponse.of(item);
    }

    @Transactional
    public void deleteItem(Long userId, Long itemId) {
        ChecklistItem item = findItemById(itemId);
        findMemberOrThrow(item.getTask().getProject().getWorkspace().getId(), userId);

        checklistItemRepository.delete(item);
    }

    @Transactional
    public void reorderItems(Long userId, Long itemId, List<Long> itemIds) {
        Task task = findTaskById(itemId);
        findMemberOrThrow(task.getProject().getWorkspace().getId(), userId);

        for(int i = 0; i < itemIds.size(); i++) {
            ChecklistItem item = findItemById(itemIds.get(i));
            item.updateOrder(i);
        }
    }

    //helper methods
    private Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));
    }

    private ChecklistItem findItemById(Long itemId) {
        return checklistItemRepository.findById(itemId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHECKLIST_ITEM_NOT_FOUND));
    }

    private WorkspaceMember findMemberOrThrow(Long workspaceId, Long userId) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_ACCESS_DENIED));
    }
}

package com.teamhub.controller;

import com.teamhub.dto.response.ApiResponse;
import com.teamhub.dto.response.ProjectResponse;
import com.teamhub.dto.response.TaskResponse;
import com.teamhub.service.TrashService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/trash")
@RequiredArgsConstructor
public class TrashController {

    private final TrashService trashService;

    @GetMapping("/tasks")
    public ApiResponse<List<TaskResponse>> getDeletedTasks(
            @PathVariable Long workspaceId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(trashService.getDeletedTasks(userId, workspaceId));
    }

    @GetMapping("/projects")
    public ApiResponse<List<ProjectResponse>> getDeletedProjects(
            @PathVariable Long workspaceId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(trashService.getDeletedProjects(userId, workspaceId));
    }

    @PostMapping("/tasks/{taskId}/restore")
    public ApiResponse<TaskResponse> restoreTask(
            @PathVariable Long workspaceId,
            @PathVariable Long taskId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("태스크가 복구되었습니다", trashService.restoreTask(userId, taskId));
    }

    @PostMapping("/projects/{projectId}/restore")
    public ApiResponse<ProjectResponse> restoreProject(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("프로젝트가 복구되었습니다", trashService.restoreProject(userId, projectId));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ApiResponse<Void> permanentDeleteTask(
            @PathVariable Long workspaceId,
            @PathVariable Long taskId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        trashService.permanentDeleteTask(userId, taskId);
        return ApiResponse.success("태스크가 영구 삭제되었습니다", null);
    }

    @DeleteMapping("/projects/{projectId}")
    public ApiResponse<Void> permanentDeleteProject(
            @PathVariable Long workspaceId,
            @PathVariable Long projectId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        trashService.permanentDeleteProject(userId, projectId);
        return ApiResponse.success("프로젝트가 영구 삭제되었습니다", null);
    }

    @DeleteMapping("/empty")
    public ApiResponse<Integer> emptyTrash(
            @PathVariable Long workspaceId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        int count = trashService.emptyTrash(userId, workspaceId);
        return ApiResponse.success("휴지통이 비워졌습니다", count);
    }
}
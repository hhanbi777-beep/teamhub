package com.teamhub.controller;

import com.teamhub.dto.request.LabelRequest;
import com.teamhub.dto.response.ApiResponse;
import com.teamhub.dto.response.LabelResponse;
import com.teamhub.service.LabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @PostMapping("/workspaces/{workspaceId}/labels")
    public ApiResponse<LabelResponse> createLabel(
            @PathVariable Long workspaceId,
            @Valid @RequestBody LabelRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("라벨 생성 성공", labelService.createLabel(userId, workspaceId, request));
    }

    @GetMapping("/workspaces/{workspaceId}/labels")
    public ApiResponse<List<LabelResponse>> getLabels(
            @PathVariable Long workspaceId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(labelService.getLabels(userId, workspaceId));
    }

    @PutMapping("/labels/{labelId}")
    public ApiResponse<LabelResponse> updateLabel(
            @PathVariable Long labelId,
            @Valid @RequestBody LabelRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("라벨 수정 성공", labelService.updateLabel(userId, labelId, request));
    }

    @DeleteMapping("/labels/{labelId}")
    public ApiResponse<Void> deleteLabel(
            @PathVariable Long labelId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        labelService.deleteLabel(userId, labelId);
        return ApiResponse.success("라벨 삭제 성공", null);
    }

    @PostMapping("/tasks/{taskId}/labels/{labelId}")
    public ApiResponse<Void> addLabelToTask(
            @PathVariable Long taskId,
            @PathVariable Long labelId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        labelService.addLabelToTask(userId, taskId, labelId);
        return ApiResponse.success("라벨 추가 성공", null);
    }

    @DeleteMapping("/tasks/{taskId}/labels/{labelId}")
    public ApiResponse<Void> removeLabelFromTask(
            @PathVariable Long taskId,
            @PathVariable Long labelId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        labelService.removeLabelFromTask(userId, taskId, labelId);
        return ApiResponse.success("라벨 제거 성공", null);
    }
}
package com.teamhub.controller;

import com.teamhub.dto.request.ChecklistItemRequest;
import com.teamhub.dto.response.ApiResponse;
import com.teamhub.dto.response.ChecklistItemResponse;
import com.teamhub.service.ChecklistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChecklistController {

    private final ChecklistService checklistService;

    @PostMapping("/tasks/{taskId}/checklist")
    public ApiResponse<ChecklistItemResponse> addItem(
            @PathVariable Long taskId,
            @Valid @RequestBody ChecklistItemRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("체크리스트 항목 추가 성공", checklistService.addItem(userId, taskId, request));
    }

    @GetMapping("/tasks/{taskId}/checklist")
    public ApiResponse<List<ChecklistItemResponse>> getItems(
            @PathVariable Long taskId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(checklistService.getItems(userId, taskId));
    }

    @PutMapping("/checklist/{itemId}")
    public ApiResponse<ChecklistItemResponse> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody ChecklistItemRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("체크리스트 항목 수정 성공", checklistService.updateItem(userId, itemId, request));
    }

    @PostMapping("/checklist/{itemId}/toggle")
    public ApiResponse<ChecklistItemResponse> toggleItem(
            @PathVariable Long itemId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("체크리스트 상태 변경 성공", checklistService.toggleItem(userId, itemId));
    }

    @DeleteMapping("/checklist/{itemId}")
    public ApiResponse<Void> deleteItem(
            @PathVariable Long itemId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        checklistService.deleteItem(userId, itemId);
        return ApiResponse.success("체크리스트 항목 삭제 성공", null);
    }

    @PostMapping("/tasks/{taskId}/checklist/reorder")
    public ApiResponse<Void> reorderItems(
            @PathVariable Long taskId,
            @RequestBody List<Long> itemIds,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        checklistService.reorderItems(userId, taskId, itemIds);
        return ApiResponse.success("체크리스트 순서 변경 성공", null);
    }
}
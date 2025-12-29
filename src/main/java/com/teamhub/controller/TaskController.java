package com.teamhub.controller;

import com.teamhub.dto.request.TaskRequest;
import com.teamhub.dto.response.ApiResponse;
import com.teamhub.dto.response.TaskResponse;
import com.teamhub.enums.project.TaskStatus;
import com.teamhub.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/create")
    public ApiResponse<TaskResponse> createTask(Authentication authentication,
                                                @RequestParam Long projectId,
                                                @Valid @RequestBody TaskRequest req) {
        Long userId = (Long) authentication.getPrincipal();
        TaskResponse res = taskService.createTask(userId, projectId, req);
        return ApiResponse.success("테스크 생성 성공", res);
    }

    @GetMapping("/list")
    public ApiResponse<List<TaskResponse>> getTasks(Authentication authentication,
                                                    @RequestParam Long projectId,
                                                    @RequestParam(required = false)TaskStatus status) {
        Long userId = (Long) authentication.getPrincipal();
        List<TaskResponse> res;

        if(status != null) {
            res = taskService.getTasksByStatus(userId, projectId, status);
        } else {
            res = taskService.getTasks(userId, projectId);
        }

        return ApiResponse.success(res);
    }

    @GetMapping("/my")
    public ApiResponse<List<TaskResponse>> getMyTasks(Authentication authentication){
        Long userId = (Long) authentication.getPrincipal();
        List<TaskResponse> res = taskService.getMyTasks(userId);
        return ApiResponse.success(res);
    }

    @GetMapping("/detail")
    public ApiResponse<TaskResponse> getTask(Authentication authentication,
                                             @RequestParam Long taskId) {
        Long userId = (Long) authentication.getPrincipal();
        TaskResponse res = taskService.getTask(userId, taskId);
        return ApiResponse.success(res);
    }

    @PostMapping("/update")
    public ApiResponse<TaskResponse> updateTask(Authentication authentication,
                                                @RequestParam Long taskId,
                                                @Valid @RequestBody TaskRequest req) {
        Long userId = (Long) authentication.getPrincipal();
        TaskResponse res = taskService.updateTask(userId, taskId, req);
        return ApiResponse.success("테스크 수정 성공", res);
    }

    @PostMapping("/change-status")
    public ApiResponse<TaskResponse> changeTaskStatus(Authentication authentication,
                                                      @RequestParam Long taskId,
                                                      @RequestParam TaskStatus status) {
        Long userId = (Long) authentication.getPrincipal();
        TaskResponse res = taskService.changeTaskStatus(userId, taskId, status);
        return ApiResponse.success("테스크 상태 변경 성공", res);
    }

    @PostMapping("/delete")
    public ApiResponse<Void> deleteTask(Authentication authentication,
                                        @RequestParam Long taskId) {
        Long userId = (Long) authentication.getPrincipal();
        taskService.deleteTask(userId, taskId);
        return ApiResponse.success("테스크 삭제 성공", null);
    }

}

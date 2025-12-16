package com.teamhub.controller;

import com.teamhub.dto.response.ActivityLogResponse;
import com.teamhub.dto.response.ApiResponse;
import com.teamhub.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityLogService activityLogService;

    @GetMapping("/list")
    public ApiResponse<List<ActivityLogResponse>> getActivities(Authentication authentication,
                                                                @RequestParam Long workspaceId,
                                                                @RequestParam(defaultValue = "20") int limit) {
        Long userId = (Long) authentication.getPrincipal();
        List<ActivityLogResponse> res = activityLogService.getActivities(userId, workspaceId, limit);
        return ApiResponse.success(res);
    }
}

package com.teamhub.controller;

import com.teamhub.dto.response.WorkspaceDashboardResponse;
import com.teamhub.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/workspaces/{workspaceId}/dashboard")
    public ResponseEntity<WorkspaceDashboardResponse> getWorkspaceDashboard(@PathVariable Long workspaceId,
                                                                            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(dashboardService.getWorkspaceDashboard(userId, workspaceId));
    }
}
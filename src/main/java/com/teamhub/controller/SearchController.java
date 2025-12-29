package com.teamhub.controller;

import com.teamhub.dto.request.ProjectSearchRequest;
import com.teamhub.dto.request.TaskSearchRequest;
import com.teamhub.dto.response.ApiResponse;
import com.teamhub.dto.response.ProjectResponse;
import com.teamhub.dto.response.TaskResponse;
import com.teamhub.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @RequestMapping("/tasks")
    public ApiResponse<List<TaskResponse>> searchTasks(Authentication authentication,
                                                       @RequestParam Long workspaceId,
                                                       @ModelAttribute TaskSearchRequest req) {

        Long userId = (Long) authentication.getPrincipal();
        List<TaskResponse> res = searchService.searchTasks(userId, workspaceId, req);

        return ApiResponse.success(res);
    }

    @RequestMapping("/projects")
    public ApiResponse<List<ProjectResponse>> searchProjects(Authentication authentication,
                                                             @RequestParam Long workspaceId,
                                                             @ModelAttribute ProjectSearchRequest req) {
        Long userId = (Long) authentication.getPrincipal();
        List<ProjectResponse> res = searchService.searchProjects(userId, workspaceId, req);

        return ApiResponse.success(res);
    }
}

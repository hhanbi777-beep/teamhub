package com.teamhub.controller;

import com.teamhub.dto.request.ProjectRequest;
import com.teamhub.dto.response.ApiResponse;
import com.teamhub.dto.response.ProjectResponse;
import com.teamhub.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/create")
    public ApiResponse<ProjectResponse> createProject(Authentication authentication, @RequestParam Long workspaceId,
                                                      @Valid @RequestBody ProjectRequest req) {
        Long userId = (Long) authentication.getPrincipal();
        ProjectResponse res = projectService.createProject(userId, workspaceId, req);
        return ApiResponse.success("프로젝트 생성 성공",res);
    }

    @GetMapping("/list")
    public ApiResponse<List<ProjectResponse>> getProjects(Authentication authentication, @RequestParam Long workspaceId) {
        Long userId = (Long) authentication.getPrincipal();
        List<ProjectResponse> res = projectService.getProjects(userId, workspaceId);
        return ApiResponse.success(res);
    }

    @GetMapping("/detail")
    public ApiResponse<ProjectResponse> getProject(Authentication authentication, @RequestParam Long projectId) {
        Long userId = (Long) authentication.getPrincipal();
        ProjectResponse res = projectService.getProject(userId, projectId);
        return ApiResponse.success(res);
    }

    @PostMapping("/update")
    public ApiResponse<ProjectResponse> updateProject(Authentication authentication,
                                                      @RequestParam Long projectId,
                                                      @Valid @RequestBody ProjectRequest req) {
        Long userId = (Long) authentication.getPrincipal();
        ProjectResponse res = projectService.updateProject(userId, projectId, req);
        return ApiResponse.success("프로젝트 수정 성공",res);
    }

    @PostMapping
    public ApiResponse<Void> deleteProject(Authentication authentication, @RequestParam Long projectId) {
        Long userId = (Long) authentication.getPrincipal();
        projectService.deleteProject(userId, projectId);
        return ApiResponse.success("프로젝트 삭제 성공", null);
    }
}

package com.teamhub.controller;

import com.teamhub.dto.request.InviteMemberRequest;
import com.teamhub.dto.request.WorkspaceRequest;
import com.teamhub.dto.response.ApiResponse;
import com.teamhub.dto.response.MemberResponse;
import com.teamhub.dto.response.WorkspaceResponse;
import com.teamhub.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping("create")
    public ApiResponse<WorkspaceResponse> createWorkspace(Authentication authentication,
                                                          @Valid @RequestBody WorkspaceRequest req) {
        Long userId = (Long) authentication.getPrincipal();
        WorkspaceResponse res = workspaceService.createWorkspace(userId, req);
        return ApiResponse.success("워크스페이스 생성 성공",res);
    }

    @GetMapping("/list")
    public ApiResponse<List<WorkspaceResponse>> listWorkspaces(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<WorkspaceResponse> res = workspaceService.getMyWorkspaces(userId);
        return ApiResponse.success(res);
    }

    @GetMapping("/detail")
    public ApiResponse<WorkspaceResponse> getWorkspace(Authentication authentication,
                                                       @RequestParam Long workspaceId) {
        Long userId = (Long) authentication.getPrincipal();
        WorkspaceResponse res = workspaceService.getWorkspace(userId, workspaceId);
        return ApiResponse.success(res);
    }

    @PostMapping("/update")
    public ApiResponse<WorkspaceResponse> updateWorkspace(Authentication authentication, @RequestParam Long workspaceId,
                                                          @Valid @RequestBody WorkspaceRequest req) {
        Long userId = (Long) authentication.getPrincipal();
        WorkspaceResponse res = workspaceService.updateWorkspace(userId, workspaceId, req);
        return ApiResponse.success("워크스페이스 수정 성공", res);
    }

    @PostMapping("/delete")
    public ApiResponse<Void> deleteWorkspace(Authentication authentication, @RequestParam Long workspaceId) {
        Long userId = (Long) authentication.getPrincipal();
        workspaceService.deleteWorkspace(userId, workspaceId);
        return ApiResponse.success("워크스페이스 삭제 성공", null);
    }

    //멤버관리
    @PostMapping("/members/invite")
    public ApiResponse<MemberResponse> inviteMember(Authentication authentication, @RequestParam Long workspaceId,
                                                    @Valid @RequestBody InviteMemberRequest req){
        Long userId = (Long) authentication.getPrincipal();
        MemberResponse res = workspaceService.inviteMember(userId, workspaceId, req);
        return ApiResponse.success("멤버 초대 성공", res);
    }

    @GetMapping("/members/list")
    public ApiResponse<List<MemberResponse>> getMembers(Authentication authentication,
                                                        @RequestParam Long workspaceId) {
        Long userId = (Long) authentication.getPrincipal();
        List<MemberResponse> res = workspaceService.getMembers(userId, workspaceId);
        return ApiResponse.success(res);
    }

    @PostMapping("/members/remove")
    public ApiResponse<Void> removeMember(Authentication authentication,
                                          @RequestParam Long workspaceId, @RequestParam Long memberId) {
        Long userId = (Long) authentication.getPrincipal();
        workspaceService.removeMember(userId, workspaceId, memberId);
        return ApiResponse.success("멤버제거 성공", null);
    }

}

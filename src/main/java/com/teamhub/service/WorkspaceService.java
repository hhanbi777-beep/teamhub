package com.teamhub.service;

import com.teamhub.domain.user.User;
import com.teamhub.domain.workspace.Workspace;
import com.teamhub.domain.workspace.WorkspaceMember;
import com.teamhub.dto.request.InviteMemberRequest;
import com.teamhub.dto.request.WorkspaceRequest;
import com.teamhub.dto.response.MemberResponse;
import com.teamhub.dto.response.WorkspaceResponse;
import com.teamhub.enums.ErrorCode;
import com.teamhub.enums.workspace.WorkspaceRole;
import com.teamhub.exception.CustomException;
import com.teamhub.repository.UserRepository;
import com.teamhub.repository.WorkspaceMemberRepository;
import com.teamhub.repository.WorkspaceRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public WorkspaceResponse createWorkspace(Long userId, WorkspaceRequest req) {
        User user = findUserById(userId);

        Workspace workspace = Workspace.builder()
                .name(req.getName())
                .description(req.getDescription())
                .owner(user)
                .build();

        workspaceRepository.save(workspace);

        //생성자를 OWNER 로 등록
        WorkspaceMember workspaceMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(user)
                .role(WorkspaceRole.OWNER)
                .build();

        workspaceMemberRepository.save(workspaceMember);

        log.info("Workspace created: {} by user: {}", workspace.getName(), user.getEmail());

        return WorkspaceResponse.of(workspace, WorkspaceRole.OWNER);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceResponse> getMyWorkspaces(Long userId) {
        List<Workspace> workspaces = workspaceRepository.findAllByUserId(userId);

        return workspaces.stream()
                .map(workspace -> {
                    WorkspaceMember member = workspaceMemberRepository
                        .findByWorkspaceIdAndUserId(workspace.getId(), userId)
                        .orElseThrow();
                    return WorkspaceResponse.of(workspace, member.getRole());
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspace(Long userId, Long workspaceId) {
        Workspace workspace = findWorkspaceById(workspaceId);
        WorkspaceMember member = findMemberOrThrow(workspaceId, userId);

        return WorkspaceResponse.of(workspace, member.getRole());
    }

    @Transactional
    public WorkspaceResponse updateWorkspace(Long userId, Long workspaceId, WorkspaceRequest req) {
        Workspace workspace = findWorkspaceById(workspaceId);
        WorkspaceMember member = findMemberOrThrow(workspaceId, userId);

        if(!member.canManageProjects()) {
            throw new CustomException(ErrorCode.WORKSPACE_DELETE_DENIED);
        }
        workspace.updateInfo(req.getName(), req.getDescription());

        return WorkspaceResponse.of(workspace, member.getRole());
    }

    @Transactional
    public void deleteWorkspace(Long userId, Long workspaceId) {
        Workspace workspace = findWorkspaceById(workspaceId);
        WorkspaceMember member = findMemberOrThrow(workspaceId, userId);

        if(!member.isOwner()) {
            throw new CustomException(ErrorCode.WORKSPACE_DELETE_DENIED);
        }
        workspace.delete();
        log.info("Workspace soft delete: {}", workspaceId);
    }

    @Transactional
    public MemberResponse inviteMember(Long userId, Long workspaceId, InviteMemberRequest req) {
        WorkspaceMember inviter = findMemberOrThrow(workspaceId, userId);

        if(!inviter.canManageMembers()) {
            throw new CustomException(ErrorCode.MEMBER_INVITE_DENIED);
        }

        if(req.getRole() == WorkspaceRole.OWNER) {
            throw new CustomException(ErrorCode.CANNOT_ASSIGN_OWNER);
        }

        User invitee = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_FOUND));

        if(workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, invitee.getId())) {
            throw new CustomException(ErrorCode.MEMBER_ALREADY_EXISTS);
        }

        Workspace workspace = findWorkspaceById(workspaceId);
        User inviterUser = findUserById(userId);

        WorkspaceMember newMember = WorkspaceMember.builder()
                .workspace(workspace)
                .user(invitee)
                .role(req.getRole())
                .build();

        workspaceMemberRepository.save(newMember);

        //초대 알림 발송
        notificationService.sendMemberInvitedNotification(invitee, inviterUser, workspace.getName(), workspaceId);

        log.info("Member invited: {} to workspace: {}", invitee.getEmail(), workspaceId);

        return MemberResponse.of(newMember);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getMembers(Long userId, Long workspaceId) {
        findMemberOrThrow(workspaceId, userId);

        return workspaceMemberRepository.findAllByWorkspaceId(workspaceId)
                .stream()
                .map(MemberResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeMember(Long userId, Long workspaceId, Long memberId) {
        WorkspaceMember req = findMemberOrThrow(workspaceId, userId);

        if(!req.canManageMembers()) {
            throw new CustomException(ErrorCode.MEMBER_REMOVE_DENIED);
        }

        WorkspaceMember targetMember = workspaceMemberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if(!targetMember.isOwner()) {
            throw new CustomException(ErrorCode.CANNOT_REMOVE_OWNER);
        }

        workspaceMemberRepository.delete(targetMember);
        log.info("Member removed: {} from workspace: {}", memberId, workspaceId);
    }

    //Helper methods
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private Workspace findWorkspaceById(Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_NOT_FOUND));
    }

    private WorkspaceMember findMemberOrThrow(Long workspaceId, Long userId) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.WORKSPACE_ACCESS_DENIED));
    }

}

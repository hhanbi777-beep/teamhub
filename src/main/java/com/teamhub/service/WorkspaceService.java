package com.teamhub.service;

import com.teamhub.domain.user.User;
import com.teamhub.domain.workspace.Workspace;
import com.teamhub.domain.workspace.WorkspaceMember;
import com.teamhub.dto.request.InviteMemberRequest;
import com.teamhub.dto.request.WorkspaceRequest;
import com.teamhub.dto.response.MemberResponse;
import com.teamhub.dto.response.WorkspaceResponse;
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
            throw new CustomException("워크스페이스 삭제는 소유자만 가능합니다." , HttpStatus.FORBIDDEN);
        }
        workspace.updateInfo(req.getName(), req.getDescription());

        return WorkspaceResponse.of(workspace, member.getRole());
    }

    @Transactional
    public void deleteWorkspace(Long userId, Long workspaceId) {
        Workspace workspace = findWorkspaceById(workspaceId);
        WorkspaceMember member = findMemberOrThrow(workspaceId, userId);

        if(!member.isOwner()) {
            throw new CustomException("워크스페이스는 삭제 소유자만 가능합니다", HttpStatus.FORBIDDEN);
        }
        workspaceRepository.delete(workspace);
        log.info("Workspace delete: {}", workspaceId);
    }

    @Transactional
    public MemberResponse inviteMember(Long userId, Long workspaceId, InviteMemberRequest req) {
        WorkspaceMember inviter = findMemberOrThrow(workspaceId, userId);

        if(!inviter.canManageMembers()) {
            throw new CustomException("멤버 초대 권한이 없습니다", HttpStatus.FORBIDDEN);
        }

        if(req.getRole() == WorkspaceRole.OWNER) {
            throw new CustomException("OWNER 역할은 부여할 수 없습니다", HttpStatus.BAD_REQUEST);
        }

        User invitee = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new CustomException("해당 이메일의 사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if(workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, invitee.getId())) {
            throw new CustomException("이미 워크스페이스 멤버입니다", HttpStatus.CONFLICT);
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
            throw new CustomException("멤버 제거 권한이 없습니다", HttpStatus.FORBIDDEN);
        }

        WorkspaceMember targetMember = workspaceMemberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException("멤버를 찾을 수 없습니다", HttpStatus.NOT_FOUND));

        if(!targetMember.isOwner()) {
            throw new CustomException("소유자는 제거할 수 없습니다", HttpStatus.BAD_REQUEST);
        }

        workspaceMemberRepository.delete(targetMember);
        log.info("Member removed: {} from workspace: {}", memberId, workspaceId);
    }

    //Helper methods
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND));
    }

    private Workspace findWorkspaceById(Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new CustomException("워크스페이스를 찾을 수 없습니다", HttpStatus.NOT_FOUND));
    }

    private WorkspaceMember findMemberOrThrow(Long workspaceId, Long userId) {
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new CustomException("워크스페이스 접근 권한이 없습니다", HttpStatus.UNAUTHORIZED));
    }

}

package com.teamhub.service;

import com.teamhub.domain.activity.ActivityLog;
import com.teamhub.domain.user.User;
import com.teamhub.domain.workspace.Workspace;
import com.teamhub.dto.response.ActivityLogResonse;
import com.teamhub.enums.activity.ActivityType;
import com.teamhub.exception.CustomException;
import com.teamhub.repository.ActivityLogRepository;
import com.teamhub.repository.WorkspaceMemberRepository;
import org.springframework.data.domain.PageRequest;
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
public class ActivityLogService {

    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final ActivityLogRepository activityLogRepository;

    public void log(Workspace workspace, User actor, String activityType,
                    String targetType, Long targetId, String targetName, String details) {
        ActivityLog activityLog  = ActivityLog.builder()
                .activityType(ActivityType.valueOf(activityType))
                .workspace(workspace)
                .actor(actor)
                .targetType(targetType)
                .targetId(targetId)
                .targetName(targetName)
                .details(details)
                .build();

        activityLogRepository.save(activityLog);
    }

    @Transactional(readOnly = true)
    public List<ActivityLogResonse> getActivities(Long userId, Long workspaceId, int limit) {
        findMemberOrThrow(workspaceId, userId);
        return activityLogRepository.findByWorkspaceIdOrderByCreatedAtDesc(workspaceId, PageRequest.of(0, limit))
                .stream()
                .map(ActivityLogResonse::of)
                .collect(Collectors.toList());
    }

    //helper
    private void findMemberOrThrow(Long workspaceId, Long userId) {
        workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new CustomException("워크스페이스 접근권한이 없습니다", HttpStatus.FORBIDDEN));
    }
}

package com.teamhub.service;

import com.teamhub.domain.user.User;
import com.teamhub.domain.workspace.Workspace;
import com.teamhub.dto.response.AdminDashboardResponse;
import com.teamhub.dto.response.AdminDashboardResponse.ActiveWorkspace;
import com.teamhub.dto.response.AdminDashboardResponse.DailyStats;
import com.teamhub.dto.response.AdminDashboardResponse.RecentUser;
import com.teamhub.enums.ErrorCode;
import com.teamhub.enums.user.UserRole;
import com.teamhub.exception.CustomException;
import com.teamhub.repository.ActivityLogRepository;
import com.teamhub.repository.ProjectRepository;
import com.teamhub.repository.TaskRepository;
import com.teamhub.repository.UserRepository;
import com.teamhub.repository.WorkspaceRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public AdminDashboardResponse getAdminDashboard(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(user.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorCode.ADMIN_ACCESS_DENIED);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekAgo = now.minusDays(7);
        LocalDateTime monthAgo = now.minusMonths(1);

        // 전체 통계
        AdminDashboardResponse.OverallStats overallStats = AdminDashboardResponse.OverallStats.builder()
            .totalUsers(userRepository.count())
            .totalWorkspaces(workspaceRepository.count())
            .totalProjects(projectRepository.count())
            .totalTasks(taskRepository.count())
            .todaySignups(userRepository.countByCreatedAtAfter(todayStart))
            .weeklySignups(userRepository.countByCreatedAtAfter(weekAgo))
            .monthlySignups(userRepository.countByCreatedAtAfter(monthAgo))
            .build();

        // 일별 가입자 (최근 30일)
        List<DailyStats> dailySignups = userRepository
            .countDailySignups(monthAgo)
            .stream()
            .map(row -> DailyStats.builder()
                .date(((java.sql.Date) row[0]).toLocalDate())
                .count((Long) row[1])
                .build())
            .collect(Collectors.toList());

        // 일별 활성 사용자 (최근 30일)
        List<DailyStats> dailyActiveUsers = activityLogRepository
            .countDailyActiveUsers(monthAgo)
            .stream()
            .map(row -> DailyStats.builder()
                .date(((java.sql.Date) row[0]).toLocalDate())
                .count((Long) row[1])
                .build())
            .collect(Collectors.toList());

        // 일별 워크스페이스 생성(최근 30일)
        List<AdminDashboardResponse.DailyStats> dailyWorkspaces = workspaceRepository
            .countDailyWorkspaces(monthAgo)
            .stream()
            .map(row -> AdminDashboardResponse.DailyStats.builder()
                .date(((java.sql.Date) row[0]).toLocalDate())
                .count((Long) row[1])
                .build())
            .collect(Collectors.toList());

        // 최근 가입 사용자 (10명)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<RecentUser> recentUsers = userRepository
            .findRecentUsers(PageRequest.of(0, 10))
            .stream()
            .map(u -> RecentUser.builder()
                .id(u.getId())
                .email(u.getEmail())
                .name(u.getName())
                .createdAt(u.getCreatedAt().format(formatter))
                .build())
            .collect(Collectors.toList());

        // 활발한 워크스페이스 TOP 10
        List<ActiveWorkspace> topWorkspaces = workspaceRepository
            .findTopWorkspacesByMemberCount(PageRequest.of(0,10))
            .stream()
            .map(row -> {
                Workspace w = (Workspace) row[0];
                Long memberCount = (Long) row[1];
                Long taskCount = taskRepository.countByWorkspaceId(w.getId());

                return ActiveWorkspace.builder()
                    .id(w.getId())
                    .name(w.getName())
                    .ownerName(w.getOwner().getName())
                    .memberCount(memberCount)
                    .taskCount(taskCount)
                    .build();
            })
            .collect(Collectors.toList());

        return AdminDashboardResponse.builder()
            .overallStats(overallStats)
            .dailySignups(dailySignups)
            .dailyActiveUsers(dailyActiveUsers)
            .dailyWorkspaces(dailyWorkspaces)
            .recentUsers(recentUsers)
            .topWorkspaces(topWorkspaces)
            .build();
    }

    // helper methods
    private LocalDate convertToLocalDate(Object dateObj) {
        if(dateObj instanceof java.sql.Date) {
            return ((java.sql.Date) dateObj).toLocalDate();
        } else if(dateObj instanceof LocalDate) {
            return (LocalDate) dateObj;
        } else {
            return LocalDate.parse(dateObj.toString());
        }
    }

}

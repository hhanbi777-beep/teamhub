package com.teamhub.dto.response;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AdminDashboardResponse {

    // 전체 통계
    private OverallStats overallStats;

    // 일별 가입자 추이
    private List<DailyStats> dailySignups;

    // 일별 활성 사용자 (DAU)
    private List<DailyStats> dailyActiveUsers;

    // 워크스페이스 생성 추이
    private List<DailyStats> dailyWorkspaces;

    // 최근 가입 사용자
    private List<RecentUser> recentUsers;

    // 활발한 워크스페이스 TOP 10
    private List<ActiveWorkspace> topWorkspaces;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OverallStats {
        private Long totalUsers;
        private Long totalWorkspaces;
        private Long totalProjects;
        private Long totalTasks;
        private Long todaySignups;
        private Long weeklySignups;
        private Long monthlySignups;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class DailyStats {
        private LocalDate date;
        private Long count;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class RecentUser {
        private Long id;
        private String email;
        private String name;
        private String createdAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ActiveWorkspace {
        private Long id;
        private String name;
        private String ownerName;
        private Long memberCount;
        private Long taskCount;
    }
}

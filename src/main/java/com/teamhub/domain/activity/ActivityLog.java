package com.teamhub.domain.activity;

import com.teamhub.domain.common.BaseEntity;
import com.teamhub.domain.user.User;
import com.teamhub.domain.workspace.Workspace;
import com.teamhub.enums.activity.ActivityType;
import com.teamhub.enums.activity.TargetType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "activity_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ActivityLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    // 대상 엔티티 정보(유연하게 저장)
    private TargetType targetType; // TASK, PROJECT, MEMBER 등
    private Long targetId;
    private String targetName;

    // 추가 정보(JSON 형태로 저장 가능)
    @Column(columnDefinition = "TEXT")
    private String details;
}

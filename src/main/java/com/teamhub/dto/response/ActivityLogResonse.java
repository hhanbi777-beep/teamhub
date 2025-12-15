package com.teamhub.dto.response;

import com.teamhub.domain.activity.ActivityLog;
import com.teamhub.enums.activity.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ActivityLogResonse {

    private Long id;
    private ActivityType activityType;
    private String actorName;
    private String actorProfileImage;
    private String targetType;
    private Long targetId;
    private String targetName;
    private String details;
    private LocalDateTime createdAt;

    public static ActivityLogResonse of(ActivityLog log) {
        return ActivityLogResonse.builder()
                .id(log.getId())
                .activityType(log.getActivityType())
                .actorName(log.getActor().getName())
                .actorProfileImage(log.getActor().getProfileImage())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .targetName(log.getTargetName())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }
}

package com.teamhub.dto.response;

import com.teamhub.domain.notification.Notification;
import com.teamhub.enums.notification.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private String senderName;
    private String targetType;
    private Long targetId;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationResponse of(Notification noti) {
        return NotificationResponse.builder()
                .id(noti.getId())
                .type(noti.getType())
                .title(noti.getTitle())
                .message(noti.getMessage())
                .senderName(noti.getSender() != null ? noti.getSender().getName() : null)
                .targetType(noti.getTargetType())
                .targetId(noti.getTargetId())
                .isRead(noti.getIsRead())
                .createdAt(noti.getCreatedAt())
                .build();
    }
}

package com.teamhub.service;

import com.teamhub.domain.notification.Notification;
import com.teamhub.domain.project.Task;
import com.teamhub.domain.user.User;
import com.teamhub.dto.response.NotificationResponse;
import com.teamhub.enums.ErrorCode;
import com.teamhub.enums.notification.NotificationType;
import com.teamhub.exception.CustomException;
import com.teamhub.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    @Transactional
    public void sendTaskAssignedNotification(Task task, User assigner) {
        if (task.getAssignee() == null) return;

        Notification notification = Notification.builder()
                .type(NotificationType.TASK_ASSIGNED)
                .title("새 테스크가 배정되었습니다")
                .message(assigner.getName() + "님이 '" + task.getTitle() + "' 테스크를 배정했습니다.")
                .recipient(task.getAssignee())
                .sender(assigner)
                .targetType("TASK")
                .targetId(task.getId())
                .build();

        Notification saved = notificationRepository.save(notification);
        notificationRepository.save(notification);
        log.info("Task assigned notification sent to user" + task.getAssignee().getId());

        // 실시간 알림 전송
        sendRealTimeNotification(saved);
    }

    @Transactional
    public void sendTaskStatusChangedNotification(Task task, User changer, String oldStatus, String newStatus) {
        // 테스크 생성자에게 알림(본인 제외)
        if (task.getCreatedBy() != null && !task.getCreatedBy().getId().equals(changer.getId())) {
            Notification notification = Notification.builder()
                    .type(NotificationType.TASK_STATUS_CHANGED)
                    .title(changer.getName() + "님이 '" + task.getTitle() + "' 상태를 " + newStatus + "로 변경했습니다.")
                    .recipient(task.getCreatedBy())
                    .sender(changer)
                    .targetType("TASK")
                    .targetId(task.getId())
                    .build();

            Notification saved = notificationRepository.save(notification);
            notificationRepository.save(notification);

            // 실시간 알림 전송
            sendRealTimeNotification(saved);
        }
    }

    public void sendCommentNotification(Task task, User commenter, String commentContent) {
        if(task.getAssignee() == null) return;

        String truncatedContent = commentContent.length() > 30
                ? commentContent.substring(0, 30) + "..."
                : commentContent;

        Notification notification = Notification.builder()
                .type(NotificationType.COMMENT_ADDED)
                .title("새 댓글이 등록되었습니다")
                .message(commenter.getName() + "님이 '" + task.getTitle() + "'에 댓글을 남겼습니다: " + truncatedContent)
                .recipient(task.getAssignee())
                .sender(commenter)
                .targetType("TASK")
                .targetId(task.getId())
                .build();

        notificationRepository.save(notification);
        log.info("Comment notification sent to user: {}", task.getAssignee().getId());
    }

    public void sendMemberInvitedNotification(User invitee, User inviter, String workspaceName, Long workspaceId) {
        Notification notification = Notification.builder()
                .type(NotificationType.MEMBER_INVITED)
                .title("워크스페이스에 초대되었습니다")
                .message(inviter.getName() + "님이 '" + workspaceName + "' 워크스페이스에 초대했습니다.")
                .recipient(invitee)
                .sender(inviter)
                .targetType("WORKSPACE")
                .targetId(workspaceId)
                .build();

        notificationRepository.save(notification);
        log.info("Member invite notification send to user: {}", invitee.getId());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userId) {
        return notificationRepository.findByRecipientId(userId)
                .stream()
                .map(NotificationResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationRepository.findUnreadByRecipientId(userId)
                .stream()
                .map(NotificationResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_ACCESS_DENIED));

        if(!notification.getRecipient().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NOTIFICATION_ACCESS_DENIED);
        }

        notification.markAsRead();
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        int updateCount = notificationRepository.markAllAsRead(userId);
        log.info("All notifications marked as read for user: {}", userId);
        return updateCount;
    }

    // 실시간 알림 전송 헬퍼 메서드
    private void sendRealTimeNotification(Notification notification) {
        try {
            webSocketNotificationService.sendToUser(
                    notification.getRecipient().getId(),
                    NotificationResponse.of(notification)
            );
        } catch (Exception e) {
            log.warn("Failed to send real-time notification: {}", e.getMessage());
            // 실시간 전송 실패해도 DB에는 저장되어 있으므로 예외 무시
        }
    }

}

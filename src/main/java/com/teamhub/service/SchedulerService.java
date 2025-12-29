package com.teamhub.service;

import com.teamhub.domain.project.Task;
import com.teamhub.domain.user.User;
import com.teamhub.domain.notification.Notification;
import com.teamhub.enums.notification.NotificationType;
import com.teamhub.repository.NotificationRepository;
import com.teamhub.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    /**
     * 매일 오전 9시에 마감 임박 태스크 알림 발송
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void sendDueDateReminders() {
        log.info("=== Starting due date reminder job ===");

        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);

        // 3일 이내 마감인 태스크 조회
        List<Task> upcomingTasks = taskRepository.findTasksDueBetween(today, threeDaysLater);

        int sentCount = 0;
        for (Task task : upcomingTasks) {
            if (task.getAssignee() != null) {
                sendDueDateNotification(task);
                sentCount++;
            }
        }

        log.info("=== Due date reminder job completed. Sent: {} notifications ===", sentCount);
    }

    private void sendDueDateNotification(Task task) {
        User assignee = task.getAssignee();
        long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), task.getDueDate());

        String message;
        if (daysUntilDue == 0) {
            message = "'" + task.getTitle() + "' 태스크가 오늘 마감입니다!";
        } else if (daysUntilDue == 1) {
            message = "'" + task.getTitle() + "' 태스크가 내일 마감입니다.";
        } else {
            message = "'" + task.getTitle() + "' 태스크가 " + daysUntilDue + "일 후 마감입니다.";
        }

        Notification notification = Notification.builder()
                .type(NotificationType.DUE_DATE_REMINDER)
                .title("마감 임박 알림")
                .message(message)
                .recipient(assignee)
                .targetType("TASK")
                .targetId(task.getId())
                .build();

        notificationRepository.save(notification);

        // 실시간 알림도 전송
        try {
            webSocketNotificationService.sendToUser(
                    assignee.getId(),
                    com.teamhub.dto.response.NotificationResponse.of(notification)
            );
        } catch (Exception e) {
            log.warn("Failed to send real-time due date notification: {}", e.getMessage());
        }
    }

    /**
     * 매주 월요일 오전 10시에 주간 요약 (필요시 구현)
     */
    @Scheduled(cron = "0 0 10 * * MON")
    public void sendWeeklySummary() {
        log.info("=== Weekly summary job triggered ===");
        // TODO: 주간 요약 로직 구현
    }
}

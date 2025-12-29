package com.teamhub.service;

import com.teamhub.dto.response.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 특정 사용자에게 알림 전송
     */
    public void sendToUser(Long userId, NotificationResponse notification) {
        String destination = "/queue/notifications";
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                destination,
                notification
        );
        log.info("WebSocket notification sent to user: {}", userId);
    }

    /**
     * 워크스페이스 전체 멤버에게 브로드캐스트
     */
    public void broadcastToWorkspace(Long workspaceId, Object message) {
        String destination = "/topic/workspace/" + workspaceId;
        messagingTemplate.convertAndSend(destination, message);
        log.info("WebSocket broadcast to workspace: {}", workspaceId);
    }

    /**
     * 프로젝트 멤버들에게 브로드캐스트
     */
    public void broadcastToProject(Long projectId, Object message) {
        String destination = "/topic/project/" + projectId;
        messagingTemplate.convertAndSend(destination, message);
    }
}

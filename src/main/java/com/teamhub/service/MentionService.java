package com.teamhub.service;

import com.teamhub.domain.notification.Notification;
import com.teamhub.domain.project.Task;
import com.teamhub.domain.user.User;
import com.teamhub.dto.response.NotificationResponse;
import com.teamhub.enums.notification.NotificationType;
import com.teamhub.repository.NotificationRepository;
import com.teamhub.repository.UserRepository;
import com.teamhub.repository.WorkspaceMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentionService {
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final NotificationRepository notificationRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    /**
     * 텍스트에서 멘션된 사용자 추출
     */
    public Set<String> extractMentions(String text) {
        Set<String> mentions = new HashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(text);

        while (matcher.find()) {
            mentions.add(matcher.group(1));
        }

        return mentions;
    }

    /**
     * 멘션된 사용자들에게 알림 발송
     */
    @Transactional
    public void processMentions(String content, Task task, User mentioner, Long workspaceId) {
        Set<String> mentionedNames = extractMentions(content);

        if(mentionedNames.isEmpty()) {
            return;
        }

        //워크스페이스 멤버 중에서 멘션된 사용자 찾기
        List<User> workspaceMemebers = workspaceMemberRepository.findAllByWorkspaceId(workspaceId)
                .stream()
                .map(member -> member.getUser())
                .toList();

        for(User member : workspaceMemebers) {
            //이름이 멘션에 포함되어있고, 본인이 아닌경우
            if(mentionedNames.contains(member.getName()) && !member.getId().equals(mentioner.getId())) {

            }
        }
    }

    private void sendMentionNotification(Task task, User mentioner, User mentioned) {
        Notification notification = Notification.builder()
                .type(NotificationType.MENTIONED)
                .title("멘션되었습니다")
                .message(mentioner.getName() + "님이 '" + task.getTitle() + "'에서 회원님을 멘션했습니다.")
                .recipient(mentioned)
                .sender(mentioner)
                .targetType("TASK")
                .targetId(task.getId())
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Mention notification sent to user: {}", mentioned.getId());

        // 실시간 알림
        try {
            webSocketNotificationService.sendToUser(mentioned.getId(), NotificationResponse.of(saved));
        } catch (Exception e) {
            log.warn("Failed to send real-time mention notification: {}", e.getMessage());
        }
    }
}

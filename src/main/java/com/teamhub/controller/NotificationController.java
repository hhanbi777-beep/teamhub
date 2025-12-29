package com.teamhub.controller;

import com.teamhub.dto.response.ApiResponse;
import com.teamhub.dto.response.NotificationResponse;
import com.teamhub.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/list")
    public ApiResponse<List<NotificationResponse>> getNotifications(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<NotificationResponse> res = notificationService.getNotifications(userId);
        return ApiResponse.success(res);
    }

    @GetMapping("/unread")
    public ApiResponse<List<NotificationResponse>> getUnreadNotifications(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<NotificationResponse> res = notificationService.getUnreadNotifications(userId);
        return ApiResponse.success(res);
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Long count = notificationService.getUnreadCount(userId);
        return ApiResponse.success(count);
    }

    @PostMapping("/read")
    public ApiResponse<Void> markAsRead(Authentication authentication,
                                        @RequestParam Long notificationId) {
        Long userId = (Long) authentication.getPrincipal();
        notificationService.markAsRead(userId, notificationId);
        return ApiResponse.success("알림 읽음 처리 성공", null);
    }

    @PostMapping("/read-all")
    public ApiResponse<Integer> markAllAsRead(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        int count = notificationService.markAllAsRead(userId);
        return ApiResponse.success("모든 알림 읽음 처리 성공 (" + count + "건)", count);
    }
}

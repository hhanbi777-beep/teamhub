package com.teamhub.domain.notification;

import com.teamhub.domain.common.BaseEntity;
import com.teamhub.domain.user.User;
import com.teamhub.enums.notification.NotificationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    //연결된 리소스 정보
    private String targetType;
    private Long targetId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    public void markAsRead() {
        this.isRead = true;
    }
}

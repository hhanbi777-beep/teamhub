package com.teamhub.repository;

import com.teamhub.domain.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId ORDER BY n.createdAt DESC")
    List<Notification> findByRecipientId(@Param("userId") Long userId);

    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByRecipientId(@Param("userId") Long userId);

    Long countByRecipientIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId);
}

package com.company.exportplatform.service;

import com.company.exportplatform.dto.response.NotificationResponse;
import com.company.exportplatform.entity.Notification;
import com.company.exportplatform.entity.User;
import com.company.exportplatform.exception.ResourceNotFoundException;
import com.company.exportplatform.repository.NotificationRepository;
import com.company.exportplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void notify(User user, com.company.exportplatform.entity.enums.NotificationType type,
                       String title, String message, String link, String entityType, Long entityId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setNotificationType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setLink(link);
        notification.setEntityType(entityType);
        notification.setEntityId(entityId);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> listForCurrentUser(String email, Pageable pageable) {
        User user = requireUser(email);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::toResponse);
    }

    @Transactional
    public void markRead(String email, Long id) {
        User user = requireUser(email);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Notification not found");
        }
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getNotificationType() == null ? null : notification.getNotificationType().name(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getLink(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}

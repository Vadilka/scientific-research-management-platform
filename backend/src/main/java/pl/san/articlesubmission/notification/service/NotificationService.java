package pl.san.articlesubmission.notification.service;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.san.articlesubmission.common.web.ResourceNotFoundException;
import pl.san.articlesubmission.notification.Notification;
import pl.san.articlesubmission.notification.NotificationType;
import pl.san.articlesubmission.notification.dto.NotificationResponse;
import pl.san.articlesubmission.notification.dto.NotificationSummaryResponse;
import pl.san.articlesubmission.notification.repository.NotificationRepository;
import pl.san.articlesubmission.submission.ArticleSubmission;
import pl.san.articlesubmission.user.User;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public NotificationService(
            NotificationRepository notificationRepository,
            NotificationMapper notificationMapper
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findCurrentUserNotifications(Authentication authentication) {
        return notificationRepository.findByRecipientEmailOrderByCreatedAtDesc(authentication.getName()).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificationSummaryResponse getSummary(Authentication authentication) {
        return new NotificationSummaryResponse(
                notificationRepository.countByRecipientEmailAndReadAtIsNull(authentication.getName())
        );
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Authentication authentication) {
        Notification notification = notificationRepository
                .findByIdAndRecipientEmail(notificationId, authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (notification.getReadAt() == null) {
            notification.setReadAt(OffsetDateTime.now());
        }

        return notificationMapper.toResponse(notification);
    }

    @Transactional
    public NotificationSummaryResponse markAllAsRead(Authentication authentication) {
        List<Notification> unreadNotifications = notificationRepository
                .findByRecipientEmailAndReadAtIsNull(authentication.getName());
        OffsetDateTime readAt = OffsetDateTime.now();

        unreadNotifications.forEach(notification -> notification.setReadAt(readAt));
        return new NotificationSummaryResponse(0);
    }

    @Transactional
    public void notifyUser(
            User recipient,
            ArticleSubmission submission,
            NotificationType notificationType,
            String title,
            String message
    ) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setSubmission(submission);
        notification.setNotificationType(notificationType);
        notification.setTitle(title);
        notification.setMessage(message);
        notificationRepository.save(notification);
    }
}

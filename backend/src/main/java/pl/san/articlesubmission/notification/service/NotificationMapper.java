package pl.san.articlesubmission.notification.service;

import org.springframework.stereotype.Component;
import pl.san.articlesubmission.notification.Notification;
import pl.san.articlesubmission.notification.dto.NotificationResponse;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getNotificationType().name(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getSubmission() == null ? null : notification.getSubmission().getId(),
                notification.getSubmission() == null ? null : notification.getSubmission().getTitle(),
                notification.getReadAt() != null,
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }
}

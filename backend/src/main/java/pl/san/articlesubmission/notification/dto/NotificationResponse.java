package pl.san.articlesubmission.notification.dto;

import java.time.OffsetDateTime;

public record NotificationResponse(
        Long id,
        String notificationType,
        String title,
        String message,
        Long submissionId,
        String submissionTitle,
        boolean read,
        OffsetDateTime readAt,
        OffsetDateTime createdAt
) {
}

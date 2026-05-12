package pl.san.articlesubmission.notification.web;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.san.articlesubmission.notification.dto.NotificationResponse;
import pl.san.articlesubmission.notification.dto.NotificationSummaryResponse;
import pl.san.articlesubmission.notification.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> findCurrentUserNotifications(Authentication authentication) {
        return notificationService.findCurrentUserNotifications(authentication);
    }

    @GetMapping("/summary")
    public NotificationSummaryResponse getSummary(Authentication authentication) {
        return notificationService.getSummary(authentication);
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication
    ) {
        return notificationService.markAsRead(notificationId, authentication);
    }

    @PatchMapping("/read-all")
    public NotificationSummaryResponse markAllAsRead(Authentication authentication) {
        return notificationService.markAllAsRead(authentication);
    }
}

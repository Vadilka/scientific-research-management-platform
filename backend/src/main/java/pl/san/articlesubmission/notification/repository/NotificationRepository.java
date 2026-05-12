package pl.san.articlesubmission.notification.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.san.articlesubmission.notification.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @EntityGraph(attributePaths = {"submission", "recipient"})
    List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String recipientEmail);

    @EntityGraph(attributePaths = {"submission", "recipient"})
    Optional<Notification> findByIdAndRecipientEmail(Long id, String recipientEmail);

    long countByRecipientEmailAndReadAtIsNull(String recipientEmail);

    List<Notification> findByRecipientEmailAndReadAtIsNull(String recipientEmail);
}

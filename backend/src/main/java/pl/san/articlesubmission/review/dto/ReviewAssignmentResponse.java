package pl.san.articlesubmission.review.dto;

import java.time.OffsetDateTime;

public record ReviewAssignmentResponse(
        Long id,
        Long submissionId,
        String submissionTitle,
        String submissionStatus,
        String categoryName,
        String reviewerEmail,
        String status,
        OffsetDateTime assignedAt,
        OffsetDateTime dueDate
) {
}

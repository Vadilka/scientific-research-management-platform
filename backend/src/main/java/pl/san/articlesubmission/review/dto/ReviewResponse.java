package pl.san.articlesubmission.review.dto;

import java.time.OffsetDateTime;

public record ReviewResponse(
        Long id,
        Long assignmentId,
        Long submissionId,
        String submissionTitle,
        String reviewerEmail,
        int score,
        String recommendation,
        String summaryComment,
        String detailedComment,
        OffsetDateTime submittedAt
) {
}

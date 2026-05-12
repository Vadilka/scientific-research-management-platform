package pl.san.articlesubmission.submission.dto;

import java.time.OffsetDateTime;

public record SubmissionSummaryResponse(
        Long id,
        String title,
        String status,
        String categoryName,
        String submittedByEmail,
        OffsetDateTime submittedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

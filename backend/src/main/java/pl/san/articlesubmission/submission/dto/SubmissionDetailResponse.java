package pl.san.articlesubmission.submission.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record SubmissionDetailResponse(
        Long id,
        String title,
        String abstractText,
        List<String> keywords,
        String correspondingAuthorEmail,
        String status,
        CategoryResponse category,
        String submittedByEmail,
        OffsetDateTime submittedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<SubmissionAuthorResponse> authors,
        List<SubmissionFileResponse> files
) {
}

package pl.san.articlesubmission.submission.dto;

import java.time.OffsetDateTime;

public record SubmissionFileResponse(
        Long id,
        String originalFileName,
        String mediaType,
        String fileType,
        long fileSize,
        OffsetDateTime createdAt
) {
}

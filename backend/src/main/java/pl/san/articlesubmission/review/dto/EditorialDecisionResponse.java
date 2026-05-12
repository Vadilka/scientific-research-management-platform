package pl.san.articlesubmission.review.dto;

import java.time.OffsetDateTime;

public record EditorialDecisionResponse(
        Long id,
        Long submissionId,
        String submissionTitle,
        String submissionStatus,
        String editorEmail,
        String decisionType,
        String decisionNote,
        OffsetDateTime decidedAt
) {
}

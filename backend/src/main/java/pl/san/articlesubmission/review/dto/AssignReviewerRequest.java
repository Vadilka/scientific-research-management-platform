package pl.san.articlesubmission.review.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record AssignReviewerRequest(
        @NotNull Long submissionId,
        @NotBlank @Email String reviewerEmail,
        OffsetDateTime dueDate
) {
}

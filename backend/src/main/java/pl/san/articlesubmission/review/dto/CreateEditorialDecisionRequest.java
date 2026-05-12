package pl.san.articlesubmission.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEditorialDecisionRequest(
        @NotNull Long submissionId,
        @NotBlank String decisionType,
        @NotBlank @Size(max = 3000) String decisionNote
) {
}

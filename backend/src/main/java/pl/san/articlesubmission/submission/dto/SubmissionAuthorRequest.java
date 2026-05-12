package pl.san.articlesubmission.submission.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SubmissionAuthorRequest(
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(max = 255) String affiliation,
        @PositiveOrZero int authorOrder,
        boolean correspondingAuthor
) {
}

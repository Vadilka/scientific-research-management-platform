package pl.san.articlesubmission.submission.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateSubmissionRequest(
        @NotBlank @Size(max = 300) String title,
        @NotBlank @Size(max = 5000) String abstractText,
        @NotEmpty List<@NotBlank @Size(max = 100) String> keywords,
        @NotBlank @Email @Size(max = 180) String correspondingAuthorEmail,
        @NotNull Long categoryId,
        @NotEmpty List<@Valid SubmissionAuthorRequest> authors,
        boolean submitNow
) {
}

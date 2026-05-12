package pl.san.articlesubmission.submission.dto;

public record SubmissionAuthorResponse(
        Long id,
        String fullName,
        String email,
        String affiliation,
        int authorOrder,
        boolean correspondingAuthor
) {
}

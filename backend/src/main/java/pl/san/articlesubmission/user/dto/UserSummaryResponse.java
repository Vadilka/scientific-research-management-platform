package pl.san.articlesubmission.user.dto;

public record UserSummaryResponse(
        Long id,
        String fullName,
        String email,
        String roleName
) {
}

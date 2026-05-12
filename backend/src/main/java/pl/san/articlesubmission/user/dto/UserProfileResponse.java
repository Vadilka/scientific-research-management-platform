package pl.san.articlesubmission.user.dto;

public record UserProfileResponse(
        Long id,
        String fullName,
        String email,
        String roleName,
        boolean enabled
) {
}

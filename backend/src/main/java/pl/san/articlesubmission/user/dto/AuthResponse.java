package pl.san.articlesubmission.user.dto;

public record AuthResponse(
        String token,
        UserProfileResponse user
) {
}

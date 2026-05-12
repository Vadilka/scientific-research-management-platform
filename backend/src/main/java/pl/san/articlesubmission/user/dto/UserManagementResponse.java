package pl.san.articlesubmission.user.dto;

public record UserManagementResponse(
        Long id,
        String fullName,
        String email,
        String roleName,
        boolean enabled
) {
}

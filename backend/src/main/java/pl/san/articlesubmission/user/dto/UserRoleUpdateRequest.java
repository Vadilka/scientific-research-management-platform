package pl.san.articlesubmission.user.dto;

import jakarta.validation.constraints.NotNull;
import pl.san.articlesubmission.user.RoleName;

public record UserRoleUpdateRequest(@NotNull RoleName roleName) {
}

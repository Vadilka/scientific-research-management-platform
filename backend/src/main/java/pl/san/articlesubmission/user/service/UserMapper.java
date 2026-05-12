package pl.san.articlesubmission.user.service;

import org.springframework.stereotype.Component;
import pl.san.articlesubmission.user.User;
import pl.san.articlesubmission.user.dto.UserManagementResponse;
import pl.san.articlesubmission.user.dto.UserProfileResponse;
import pl.san.articlesubmission.user.dto.UserSummaryResponse;

@Component
public class UserMapper {

    public UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRoleName().name(),
                user.isEnabled()
        );
    }

    public UserSummaryResponse toSummaryResponse(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRoleName().name()
        );
    }

    public UserManagementResponse toManagementResponse(User user) {
        return new UserManagementResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRoleName().name(),
                user.isEnabled()
        );
    }
}

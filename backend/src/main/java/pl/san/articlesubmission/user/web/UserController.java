package pl.san.articlesubmission.user.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.san.articlesubmission.user.RoleName;
import pl.san.articlesubmission.user.dto.UserManagementResponse;
import pl.san.articlesubmission.user.dto.UserRoleUpdateRequest;
import pl.san.articlesubmission.user.dto.UserSummaryResponse;
import pl.san.articlesubmission.user.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserSummaryResponse> findUsersByRole(@RequestParam RoleName role) {
        return userService.findUsersByRole(role);
    }

    @GetMapping("/management")
    public List<UserManagementResponse> findUsersForManagement() {
        return userService.findUsersForManagement();
    }

    @PatchMapping("/{userId}/role")
    public UserManagementResponse updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UserRoleUpdateRequest request,
            Authentication authentication
    ) {
        return userService.updateUserRole(userId, request.roleName(), authentication);
    }
}

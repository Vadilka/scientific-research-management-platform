package pl.san.articlesubmission.user.service;

import java.util.List;
import java.util.Locale;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.san.articlesubmission.common.web.BusinessRuleViolationException;
import pl.san.articlesubmission.common.web.ResourceNotFoundException;
import pl.san.articlesubmission.user.RoleName;
import pl.san.articlesubmission.user.User;
import pl.san.articlesubmission.user.dto.UserManagementResponse;
import pl.san.articlesubmission.user.dto.UserProfileResponse;
import pl.san.articlesubmission.user.dto.UserRegistrationRequest;
import pl.san.articlesubmission.user.dto.UserSummaryResponse;
import pl.san.articlesubmission.user.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .map(userMapper::toProfileResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    @Transactional
    public UserProfileResponse register(UserRegistrationRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessRuleViolationException("A user with this email already exists");
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRoleName(RoleName.AUTHOR);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        return userMapper.toProfileResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> findUsersByRole(RoleName roleName) {
        return userRepository.findByRoleNameOrderByFullNameAsc(roleName).stream()
                .map(userMapper::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserManagementResponse> findUsersForManagement() {
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(userMapper::toManagementResponse)
                .toList();
    }

    @Transactional
    public UserManagementResponse updateUserRole(Long userId, RoleName roleName, Authentication authentication) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (authentication.getName().equalsIgnoreCase(user.getEmail()) && user.getRoleName() != roleName) {
            throw new BusinessRuleViolationException("Administrators cannot change their own role");
        }

        user.setRoleName(roleName);
        return userMapper.toManagementResponse(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

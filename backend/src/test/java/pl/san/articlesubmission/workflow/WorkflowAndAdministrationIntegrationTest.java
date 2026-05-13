package pl.san.articlesubmission.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.san.articlesubmission.common.web.BusinessRuleViolationException;
import pl.san.articlesubmission.notification.dto.NotificationResponse;
import pl.san.articlesubmission.notification.service.NotificationService;
import pl.san.articlesubmission.reporting.dto.ReportSummaryResponse;
import pl.san.articlesubmission.reporting.service.ReportingService;
import pl.san.articlesubmission.review.dto.AssignReviewerRequest;
import pl.san.articlesubmission.review.dto.CreateEditorialDecisionRequest;
import pl.san.articlesubmission.review.dto.CreateReviewRequest;
import pl.san.articlesubmission.review.dto.ReviewAssignmentResponse;
import pl.san.articlesubmission.review.service.EditorialDecisionService;
import pl.san.articlesubmission.review.service.ReviewAssignmentService;
import pl.san.articlesubmission.review.service.ReviewService;
import pl.san.articlesubmission.submission.ArticleStatus;
import pl.san.articlesubmission.submission.ScientificCategory;
import pl.san.articlesubmission.submission.dto.CreateSubmissionRequest;
import pl.san.articlesubmission.submission.dto.SubmissionAuthorRequest;
import pl.san.articlesubmission.submission.dto.SubmissionDetailResponse;
import pl.san.articlesubmission.submission.repository.ScientificCategoryRepository;
import pl.san.articlesubmission.submission.service.SubmissionService;
import pl.san.articlesubmission.user.RoleName;
import pl.san.articlesubmission.user.User;
import pl.san.articlesubmission.user.dto.UserProfileResponse;
import pl.san.articlesubmission.user.dto.UserRegistrationRequest;
import pl.san.articlesubmission.user.repository.UserRepository;
import pl.san.articlesubmission.user.service.UserService;

@SpringBootTest
class WorkflowAndAdministrationIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private ReviewAssignmentService reviewAssignmentService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private EditorialDecisionService editorialDecisionService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ReportingService reportingService;

    @Autowired
    private ScientificCategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void coversManualRolesReviewNotificationsAndReportExports() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User admin = createUser("Admin " + suffix, "admin." + suffix + "@san.local", RoleName.ADMIN);
        User editor = createUser("Editor " + suffix, "editor." + suffix + "@san.local", RoleName.EDITOR);
        User author = createUser("Author " + suffix, "author." + suffix + "@san.local", RoleName.AUTHOR);
        ScientificCategory category = createCategory("WF-" + suffix, "Workflow Tests " + suffix);
        ReportSummaryResponse before = reportingService.buildSummary();

        UserProfileResponse registeredReviewer = userService.register(new UserRegistrationRequest(
                "Registered Reviewer",
                "Reviewer." + suffix + "@SAN.LOCAL",
                "password123"
        ));

        assertThat(registeredReviewer.roleName()).isEqualTo(RoleName.AUTHOR.name());

        assertThat(userService.updateUserRole(
                registeredReviewer.id(),
                RoleName.REVIEWER,
                authentication(admin.getEmail(), "ROLE_ADMIN")
        ).roleName()).isEqualTo(RoleName.REVIEWER.name());

        assertThatThrownBy(() -> userService.updateUserRole(
                admin.getId(),
                RoleName.AUTHOR,
                authentication(admin.getEmail(), "ROLE_ADMIN")
        )).isInstanceOf(BusinessRuleViolationException.class);

        SubmissionDetailResponse submitted = submissionService.create(new CreateSubmissionRequest(
                "Workflow article " + suffix,
                "A complete workflow integration test article.",
                List.of("workflow", "notifications", "exports"),
                author.getEmail(),
                category.getId(),
                List.of(authorRequest("Author " + suffix, author.getEmail())),
                true
        ), authentication(author.getEmail(), "ROLE_AUTHOR"));

        assertThat(submitted.status()).isEqualTo(ArticleStatus.SUBMITTED.name());

        ReviewAssignmentResponse assignment = reviewAssignmentService.assignReviewer(new AssignReviewerRequest(
                submitted.id(),
                registeredReviewer.email(),
                OffsetDateTime.now().plusDays(7)
        ));

        assertThat(assignment.status()).isEqualTo("ASSIGNED");
        assertThat(submissionService.findById(submitted.id()).status()).isEqualTo(ArticleStatus.IN_REVIEW.name());

        reviewService.submitReview(new CreateReviewRequest(
                assignment.id(),
                9,
                "ACCEPT",
                "The article is ready for acceptance.",
                "The structure, metadata, and workflow are correct."
        ), authentication(registeredReviewer.email(), "ROLE_REVIEWER"));

        assertThat(submissionService.findById(submitted.id()).status())
                .isEqualTo(ArticleStatus.REVIEW_COMPLETED.name());

        editorialDecisionService.createDecision(new CreateEditorialDecisionRequest(
                submitted.id(),
                "ACCEPT",
                "The article satisfies the publication criteria."
        ), authentication(editor.getEmail(), "ROLE_EDITOR"));

        assertThat(submissionService.findById(submitted.id()).status()).isEqualTo(ArticleStatus.ACCEPTED.name());

        editorialDecisionService.createDecision(new CreateEditorialDecisionRequest(
                submitted.id(),
                "PUBLISH",
                "The accepted article is published."
        ), authentication(editor.getEmail(), "ROLE_EDITOR"));

        assertThat(submissionService.findById(submitted.id()).status()).isEqualTo(ArticleStatus.PUBLISHED.name());

        List<NotificationResponse> authorNotifications = notificationService.findCurrentUserNotifications(
                authentication(author.getEmail(), "ROLE_AUTHOR")
        );
        assertThat(authorNotifications)
                .extracting(NotificationResponse::notificationType)
                .contains(
                        "REVIEW_ASSIGNED",
                        "REVIEW_SUBMITTED",
                        "REVIEW_COMPLETED",
                        "EDITORIAL_DECISION",
                        "PUBLICATION"
                );

        assertThat(notificationService.getSummary(authentication(author.getEmail(), "ROLE_AUTHOR")).unreadCount())
                .isGreaterThanOrEqualTo(5);

        notificationService.markAsRead(authorNotifications.get(0).id(), authentication(author.getEmail(), "ROLE_AUTHOR"));
        notificationService.markAllAsRead(authentication(author.getEmail(), "ROLE_AUTHOR"));

        assertThat(notificationService.getSummary(authentication(author.getEmail(), "ROLE_AUTHOR")).unreadCount())
                .isZero();

        ReportSummaryResponse after = reportingService.buildSummary();
        assertThat(after.totalSubmissions()).isEqualTo(before.totalSubmissions() + 1);
        assertThat(after.publishedSubmissions()).isEqualTo(before.publishedSubmissions() + 1);
        assertThat(after.totalReviewAssignments()).isEqualTo(before.totalReviewAssignments() + 1);
        assertThat(after.submittedReviewAssignments()).isEqualTo(before.submittedReviewAssignments() + 1);

        String csv = new String(reportingService.exportSubmissionsAsCsv(), StandardCharsets.UTF_8);
        assertThat(csv).contains("Workflow article " + suffix);
        assertThat(reportingService.exportSubmissionsAsPdf()).startsWith("%PDF".getBytes(StandardCharsets.US_ASCII));
    }

    @Test
    void rejectsDuplicateReviewerAssignmentForTheSameSubmission() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User author = createUser("Duplicate Author " + suffix, "duplicate.author." + suffix + "@san.local", RoleName.AUTHOR);
        User reviewer = createUser("Duplicate Reviewer " + suffix, "duplicate.reviewer." + suffix + "@san.local", RoleName.REVIEWER);
        ScientificCategory category = createCategory("DUP-" + suffix, "Duplicate Assignment " + suffix);

        SubmissionDetailResponse submitted = submissionService.create(new CreateSubmissionRequest(
                "Duplicate assignment article " + suffix,
                "A submitted article should not receive the same reviewer twice.",
                List.of("review", "assignment"),
                author.getEmail(),
                category.getId(),
                List.of(authorRequest("Duplicate Author " + suffix, author.getEmail())),
                true
        ), authentication(author.getEmail(), "ROLE_AUTHOR"));

        reviewAssignmentService.assignReviewer(new AssignReviewerRequest(
                submitted.id(),
                reviewer.getEmail(),
                OffsetDateTime.now().plusDays(5)
        ));

        assertThatThrownBy(() -> reviewAssignmentService.assignReviewer(new AssignReviewerRequest(
                submitted.id(),
                reviewer.getEmail(),
                OffsetDateTime.now().plusDays(6)
        )))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Reviewer is already assigned to this submission");
    }

    private User createUser(String fullName, String email, RoleName roleName) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash("{noop}password");
        user.setRoleName(roleName);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private ScientificCategory createCategory(String code, String name) {
        ScientificCategory category = new ScientificCategory();
        category.setCode(code);
        category.setName(name);
        category.setDescription("Integration test category");
        return categoryRepository.save(category);
    }

    private SubmissionAuthorRequest authorRequest(String fullName, String email) {
        return new SubmissionAuthorRequest(
                fullName,
                email,
                "Społeczna Akademia Nauk",
                0,
                true
        );
    }

    private Authentication authentication(String email, String role) {
        return new UsernamePasswordAuthenticationToken(
                email,
                "password",
                List.of(new SimpleGrantedAuthority(role))
        );
    }
}

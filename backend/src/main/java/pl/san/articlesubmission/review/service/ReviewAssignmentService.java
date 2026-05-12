package pl.san.articlesubmission.review.service;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.san.articlesubmission.common.web.BusinessRuleViolationException;
import pl.san.articlesubmission.common.web.ResourceNotFoundException;
import pl.san.articlesubmission.notification.NotificationType;
import pl.san.articlesubmission.notification.service.NotificationService;
import pl.san.articlesubmission.review.ReviewAssignment;
import pl.san.articlesubmission.review.ReviewAssignmentStatus;
import pl.san.articlesubmission.review.dto.AssignReviewerRequest;
import pl.san.articlesubmission.review.dto.ReviewAssignmentResponse;
import pl.san.articlesubmission.review.repository.ReviewAssignmentRepository;
import pl.san.articlesubmission.submission.ArticleStatus;
import pl.san.articlesubmission.submission.ArticleSubmission;
import pl.san.articlesubmission.submission.repository.ArticleSubmissionRepository;
import pl.san.articlesubmission.user.RoleName;
import pl.san.articlesubmission.user.User;
import pl.san.articlesubmission.user.repository.UserRepository;

@Service
public class ReviewAssignmentService {

    private final ReviewAssignmentRepository reviewAssignmentRepository;
    private final ArticleSubmissionRepository articleSubmissionRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;
    private final NotificationService notificationService;

    public ReviewAssignmentService(
            ReviewAssignmentRepository reviewAssignmentRepository,
            ArticleSubmissionRepository articleSubmissionRepository,
            UserRepository userRepository,
            ReviewMapper reviewMapper,
            NotificationService notificationService
    ) {
        this.reviewAssignmentRepository = reviewAssignmentRepository;
        this.articleSubmissionRepository = articleSubmissionRepository;
        this.userRepository = userRepository;
        this.reviewMapper = reviewMapper;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<ReviewAssignmentResponse> findVisibleAssignments(Authentication authentication) {
        List<ReviewAssignment> assignments;
        if (hasAnyRole(authentication, "ROLE_EDITOR", "ROLE_ADMIN")) {
            assignments = reviewAssignmentRepository.findAll();
        } else {
            assignments = reviewAssignmentRepository.findByReviewerEmail(authentication.getName());
        }

        return assignments.stream()
                .map(reviewMapper::toAssignmentResponse)
                .toList();
    }

    @Transactional
    public ReviewAssignmentResponse assignReviewer(AssignReviewerRequest request) {
        ArticleSubmission submission = articleSubmissionRepository.findById(request.submissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        if (submission.getStatus() != ArticleStatus.SUBMITTED && submission.getStatus() != ArticleStatus.IN_REVIEW) {
            throw new BusinessRuleViolationException(
                    "Reviewers can only be assigned to submissions that are submitted or already in review"
            );
        }

        User reviewer = userRepository.findByEmail(request.reviewerEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));

        if (reviewer.getRoleName() != RoleName.REVIEWER) {
            throw new BusinessRuleViolationException("Selected user does not have the REVIEWER role");
        }

        if (reviewAssignmentRepository.existsBySubmissionIdAndReviewerId(submission.getId(), reviewer.getId())) {
            throw new BusinessRuleViolationException("Reviewer is already assigned to this submission");
        }

        ReviewAssignment assignment = new ReviewAssignment();
        assignment.setSubmission(submission);
        assignment.setReviewer(reviewer);
        assignment.setStatus(ReviewAssignmentStatus.ASSIGNED);
        assignment.setDueDate(request.dueDate());

        submission.setStatus(ArticleStatus.IN_REVIEW);

        ReviewAssignment savedAssignment = reviewAssignmentRepository.save(assignment);
        notificationService.notifyUser(
                submission.getSubmittedBy(),
                submission,
                NotificationType.REVIEW_ASSIGNED,
                "Submission moved to review",
                "Your submission \"" + submission.getTitle() + "\" has been assigned to a reviewer."
        );
        notificationService.notifyUser(
                reviewer,
                submission,
                NotificationType.REVIEW_ASSIGNED,
                "New review assignment",
                "You have been assigned to review \"" + submission.getTitle() + "\"."
        );
        return reviewMapper.toAssignmentResponse(savedAssignment);
    }

    private boolean hasAnyRole(Authentication authentication, String... roles) {
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            for (String role : roles) {
                if (role.equals(authority.getAuthority())) {
                    return true;
                }
            }
        }
        return false;
    }
}

package pl.san.articlesubmission.review.service;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.san.articlesubmission.common.web.BusinessRuleViolationException;
import pl.san.articlesubmission.common.web.ResourceNotFoundException;
import pl.san.articlesubmission.notification.NotificationType;
import pl.san.articlesubmission.notification.service.NotificationService;
import pl.san.articlesubmission.review.Review;
import pl.san.articlesubmission.review.ReviewAssignment;
import pl.san.articlesubmission.review.ReviewAssignmentStatus;
import pl.san.articlesubmission.review.ReviewRecommendation;
import pl.san.articlesubmission.review.dto.CreateReviewRequest;
import pl.san.articlesubmission.review.dto.ReviewResponse;
import pl.san.articlesubmission.review.repository.ReviewAssignmentRepository;
import pl.san.articlesubmission.review.repository.ReviewRepository;
import pl.san.articlesubmission.submission.ArticleStatus;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewAssignmentRepository reviewAssignmentRepository;
    private final ReviewMapper reviewMapper;
    private final NotificationService notificationService;

    public ReviewService(
            ReviewRepository reviewRepository,
            ReviewAssignmentRepository reviewAssignmentRepository,
            ReviewMapper reviewMapper,
            NotificationService notificationService
    ) {
        this.reviewRepository = reviewRepository;
        this.reviewAssignmentRepository = reviewAssignmentRepository;
        this.reviewMapper = reviewMapper;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> findBySubmissionId(Long submissionId) {
        return reviewRepository.findByAssignmentSubmissionId(submissionId).stream()
                .map(reviewMapper::toReviewResponse)
                .toList();
    }

    @Transactional
    public ReviewResponse submitReview(CreateReviewRequest request, Authentication authentication) {
        ReviewAssignment assignment = reviewAssignmentRepository.findById(request.assignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Review assignment not found"));

        if (!assignment.getReviewer().getEmail().equals(authentication.getName())) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
            if (!isAdmin) {
                throw new BusinessRuleViolationException("You can only submit reviews for your own assignments");
            }
        }

        if (reviewRepository.existsByAssignmentId(assignment.getId())) {
            throw new BusinessRuleViolationException("A review for this assignment already exists");
        }

        Review review = new Review();
        review.setAssignment(assignment);
        review.setScore(request.score());
        review.setRecommendation(parseRecommendation(request.recommendation()));
        review.setSummaryComment(request.summaryComment());
        review.setDetailedComment(request.detailedComment());
        review.setSubmittedAt(OffsetDateTime.now());

        assignment.setStatus(ReviewAssignmentStatus.SUBMITTED);

        Review savedReview = reviewRepository.save(review);
        notificationService.notifyUser(
                assignment.getSubmission().getSubmittedBy(),
                assignment.getSubmission(),
                NotificationType.REVIEW_SUBMITTED,
                "Review submitted",
                "A review has been submitted for \"" + assignment.getSubmission().getTitle() + "\"."
        );

        long totalAssignments = reviewAssignmentRepository.countBySubmissionId(assignment.getSubmission().getId());
        long submittedAssignments = reviewAssignmentRepository.countBySubmissionIdAndStatus(
                assignment.getSubmission().getId(),
                ReviewAssignmentStatus.SUBMITTED
        );

        if (totalAssignments > 0 && totalAssignments == submittedAssignments) {
            assignment.getSubmission().setStatus(ArticleStatus.REVIEW_COMPLETED);
            notificationService.notifyUser(
                    assignment.getSubmission().getSubmittedBy(),
                    assignment.getSubmission(),
                    NotificationType.REVIEW_COMPLETED,
                    "Reviews completed",
                    "All reviews for \"" + assignment.getSubmission().getTitle() + "\" have been completed."
            );
        } else {
            assignment.getSubmission().setStatus(ArticleStatus.IN_REVIEW);
        }

        return reviewMapper.toReviewResponse(savedReview);
    }

    private ReviewRecommendation parseRecommendation(String rawRecommendation) {
        try {
            return ReviewRecommendation.valueOf(rawRecommendation.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid review recommendation value");
        }
    }
}

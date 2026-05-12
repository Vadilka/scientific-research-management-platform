package pl.san.articlesubmission.review.service;

import org.springframework.stereotype.Component;
import pl.san.articlesubmission.review.EditorialDecision;
import pl.san.articlesubmission.review.Review;
import pl.san.articlesubmission.review.ReviewAssignment;
import pl.san.articlesubmission.review.dto.EditorialDecisionResponse;
import pl.san.articlesubmission.review.dto.ReviewAssignmentResponse;
import pl.san.articlesubmission.review.dto.ReviewResponse;

@Component
public class ReviewMapper {

    public ReviewAssignmentResponse toAssignmentResponse(ReviewAssignment assignment) {
        return new ReviewAssignmentResponse(
                assignment.getId(),
                assignment.getSubmission().getId(),
                assignment.getSubmission().getTitle(),
                assignment.getSubmission().getStatus().name(),
                assignment.getSubmission().getCategory().getName(),
                assignment.getReviewer().getEmail(),
                assignment.getStatus().name(),
                assignment.getAssignedAt(),
                assignment.getDueDate()
        );
    }

    public ReviewResponse toReviewResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getAssignment().getId(),
                review.getAssignment().getSubmission().getId(),
                review.getAssignment().getSubmission().getTitle(),
                review.getAssignment().getReviewer().getEmail(),
                review.getScore(),
                review.getRecommendation().name(),
                review.getSummaryComment(),
                review.getDetailedComment(),
                review.getSubmittedAt()
        );
    }

    public EditorialDecisionResponse toEditorialDecisionResponse(EditorialDecision decision) {
        return new EditorialDecisionResponse(
                decision.getId(),
                decision.getSubmission().getId(),
                decision.getSubmission().getTitle(),
                decision.getSubmission().getStatus().name(),
                decision.getEditor().getEmail(),
                decision.getDecisionType().name(),
                decision.getDecisionNote(),
                decision.getDecidedAt()
        );
    }
}

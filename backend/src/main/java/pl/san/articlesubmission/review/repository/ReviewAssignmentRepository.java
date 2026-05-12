package pl.san.articlesubmission.review.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.san.articlesubmission.review.ReviewAssignment;
import pl.san.articlesubmission.review.ReviewAssignmentStatus;

public interface ReviewAssignmentRepository extends JpaRepository<ReviewAssignment, Long> {

    @Override
    @EntityGraph(attributePaths = {"submission", "submission.category", "reviewer"})
    List<ReviewAssignment> findAll();

    @Override
    @EntityGraph(attributePaths = {"submission", "submission.category", "reviewer"})
    Optional<ReviewAssignment> findById(Long id);

    @EntityGraph(attributePaths = {"submission", "submission.category", "reviewer"})
    List<ReviewAssignment> findByReviewerEmail(String reviewerEmail);

    boolean existsBySubmissionIdAndReviewerId(Long submissionId, Long reviewerId);

    long countBySubmissionId(Long submissionId);

    long countBySubmissionIdAndStatus(Long submissionId, ReviewAssignmentStatus status);

    long countByStatus(ReviewAssignmentStatus status);
}

package pl.san.articlesubmission.review.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.san.articlesubmission.review.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Override
    @EntityGraph(attributePaths = {"assignment", "assignment.submission", "assignment.reviewer"})
    Optional<Review> findById(Long id);

    @EntityGraph(attributePaths = {"assignment", "assignment.submission", "assignment.reviewer"})
    List<Review> findByAssignmentSubmissionId(Long submissionId);

    boolean existsByAssignmentId(Long assignmentId);
}

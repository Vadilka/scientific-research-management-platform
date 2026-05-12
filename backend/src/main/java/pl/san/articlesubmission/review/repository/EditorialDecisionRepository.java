package pl.san.articlesubmission.review.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.san.articlesubmission.review.EditorialDecision;

public interface EditorialDecisionRepository extends JpaRepository<EditorialDecision, Long> {

    List<EditorialDecision> findBySubmissionIdOrderByDecidedAtDesc(Long submissionId);
}

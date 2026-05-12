package pl.san.articlesubmission.submission.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.san.articlesubmission.submission.SubmissionFile;

public interface SubmissionFileRepository extends JpaRepository<SubmissionFile, Long> {

    List<SubmissionFile> findBySubmissionIdOrderByCreatedAtDesc(Long submissionId);

    Optional<SubmissionFile> findByIdAndSubmissionId(Long id, Long submissionId);
}

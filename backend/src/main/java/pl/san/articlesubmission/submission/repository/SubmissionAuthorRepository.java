package pl.san.articlesubmission.submission.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.san.articlesubmission.submission.SubmissionAuthor;

public interface SubmissionAuthorRepository extends JpaRepository<SubmissionAuthor, Long> {

    List<SubmissionAuthor> findBySubmissionIdOrderByAuthorOrderAsc(Long submissionId);

    void deleteBySubmissionId(Long submissionId);
}

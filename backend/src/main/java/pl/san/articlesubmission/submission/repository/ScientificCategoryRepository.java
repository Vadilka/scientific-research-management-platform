package pl.san.articlesubmission.submission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.san.articlesubmission.submission.ScientificCategory;

public interface ScientificCategoryRepository extends JpaRepository<ScientificCategory, Long> {
}

package pl.san.articlesubmission.submission.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.san.articlesubmission.submission.dto.CategoryResponse;
import pl.san.articlesubmission.submission.repository.ScientificCategoryRepository;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final ScientificCategoryRepository scientificCategoryRepository;
    private final SubmissionMapper submissionMapper;

    public CategoryService(
            ScientificCategoryRepository scientificCategoryRepository,
            SubmissionMapper submissionMapper
    ) {
        this.scientificCategoryRepository = scientificCategoryRepository;
        this.submissionMapper = submissionMapper;
    }

    public List<CategoryResponse> findAll() {
        return scientificCategoryRepository.findAll().stream()
                .map(submissionMapper::toCategoryResponse)
                .toList();
    }
}

package pl.san.articlesubmission.submission.service;

import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;
import pl.san.articlesubmission.submission.ArticleSubmission;
import pl.san.articlesubmission.submission.ScientificCategory;
import pl.san.articlesubmission.submission.SubmissionAuthor;
import pl.san.articlesubmission.submission.SubmissionFile;
import pl.san.articlesubmission.submission.dto.CategoryResponse;
import pl.san.articlesubmission.submission.dto.SubmissionAuthorResponse;
import pl.san.articlesubmission.submission.dto.SubmissionDetailResponse;
import pl.san.articlesubmission.submission.dto.SubmissionFileResponse;
import pl.san.articlesubmission.submission.dto.SubmissionSummaryResponse;

@Component
public class SubmissionMapper {

    public CategoryResponse toCategoryResponse(ScientificCategory category) {
        return new CategoryResponse(
                category.getId(),
                category.getCode(),
                category.getName(),
                category.getDescription()
        );
    }

    public SubmissionSummaryResponse toSummaryResponse(ArticleSubmission submission) {
        return new SubmissionSummaryResponse(
                submission.getId(),
                submission.getTitle(),
                submission.getStatus().name(),
                submission.getCategory().getName(),
                submission.getSubmittedBy().getEmail(),
                submission.getSubmittedAt(),
                submission.getCreatedAt(),
                submission.getUpdatedAt()
        );
    }

    public SubmissionDetailResponse toDetailResponse(
            ArticleSubmission submission,
            List<SubmissionAuthor> authors,
            List<SubmissionFile> files
    ) {
        return new SubmissionDetailResponse(
                submission.getId(),
                submission.getTitle(),
                submission.getAbstractText(),
                splitKeywords(submission.getKeywords()),
                submission.getCorrespondingAuthorEmail(),
                submission.getStatus().name(),
                toCategoryResponse(submission.getCategory()),
                submission.getSubmittedBy().getEmail(),
                submission.getSubmittedAt(),
                submission.getCreatedAt(),
                submission.getUpdatedAt(),
                authors.stream()
                        .map(author -> new SubmissionAuthorResponse(
                                author.getId(),
                                author.getFullName(),
                                author.getEmail(),
                                author.getAffiliation(),
                                author.getAuthorOrder(),
                                author.isCorrespondingAuthor()
                        ))
                        .toList(),
                files.stream()
                        .map(file -> new SubmissionFileResponse(
                                file.getId(),
                                file.getOriginalFileName(),
                                file.getMediaType(),
                                file.getFileType().name(),
                                file.getFileSize(),
                                file.getCreatedAt()
                        ))
                        .toList()
        );
    }

    public String joinKeywords(List<String> keywords) {
        return String.join(", ", keywords);
    }

    private List<String> splitKeywords(String keywords) {
        if (keywords == null || keywords.isBlank()) {
            return List.of();
        }

        return Arrays.stream(keywords.split("\\s*,\\s*"))
                .filter(keyword -> !keyword.isBlank())
                .toList();
    }
}

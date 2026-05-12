package pl.san.articlesubmission.monitoring;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.san.articlesubmission.review.ReviewAssignmentStatus;
import pl.san.articlesubmission.review.repository.ReviewAssignmentRepository;
import pl.san.articlesubmission.submission.ArticleStatus;
import pl.san.articlesubmission.submission.repository.ArticleSubmissionRepository;
import pl.san.articlesubmission.submission.repository.SubmissionFileRepository;

@Configuration
public class MonitoringConfiguration {

    @Bean
    MeterBinder workflowMetricsBinder(
            ArticleSubmissionRepository articleSubmissionRepository,
            ReviewAssignmentRepository reviewAssignmentRepository,
            SubmissionFileRepository submissionFileRepository
    ) {
        return registry -> {
            Gauge.builder("article_submission_submissions_total", articleSubmissionRepository, repo -> (double) repo.count())
                    .description("Total number of article submissions")
                    .register(registry);

            for (ArticleStatus status : ArticleStatus.values()) {
                Gauge.builder(
                                "article_submission_submissions_status",
                                articleSubmissionRepository,
                                repo -> (double) repo.countByStatus(status)
                        )
                        .description("Number of article submissions by status")
                        .tag("status", status.name())
                        .register(registry);
            }

            Gauge.builder(
                            "article_submission_review_assignments_total",
                            reviewAssignmentRepository,
                            repo -> (double) repo.count()
                    )
                    .description("Total number of review assignments")
                    .register(registry);

            for (ReviewAssignmentStatus status : ReviewAssignmentStatus.values()) {
                Gauge.builder(
                                "article_submission_review_assignments_status",
                                reviewAssignmentRepository,
                                repo -> (double) repo.countByStatus(status)
                        )
                        .description("Number of review assignments by status")
                        .tag("status", status.name())
                        .register(registry);
            }

            Gauge.builder("article_submission_files_total", submissionFileRepository, repo -> (double) repo.count())
                    .description("Total number of uploaded submission files")
                    .register(registry);
        };
    }
}

package pl.san.articlesubmission.submission.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.san.articlesubmission.submission.ArticleStatus;
import pl.san.articlesubmission.submission.ArticleSubmission;

public interface ArticleSubmissionRepository extends JpaRepository<ArticleSubmission, Long> {

    @Override
    @EntityGraph(attributePaths = {"category", "submittedBy"})
    List<ArticleSubmission> findAll();

    @Override
    @EntityGraph(attributePaths = {"category", "submittedBy"})
    Optional<ArticleSubmission> findById(Long id);

    @Query("""
            select distinct submission
            from ArticleSubmission submission
            join fetch submission.category category
            join fetch submission.submittedBy submittedBy
            where (:status is null or submission.status = :status)
              and (:categoryId is null or category.id = :categoryId)
            order by submission.createdAt desc
            """)
    List<ArticleSubmission> searchWithoutText(
            @Param("status") ArticleStatus status,
            @Param("categoryId") Long categoryId
    );

    @Query("""
            select distinct submission
            from ArticleSubmission submission
            join fetch submission.category category
            join fetch submission.submittedBy submittedBy
            where (:status is null or submission.status = :status)
              and (:categoryId is null or category.id = :categoryId)
              and (
                    :query is null
                    or lower(submission.title) like lower(concat('%', :query, '%'))
                    or lower(submission.abstractText) like lower(concat('%', :query, '%'))
                    or lower(submission.keywords) like lower(concat('%', :query, '%'))
                    or lower(submittedBy.email) like lower(concat('%', :query, '%'))
                    or exists (
                        select author.id
                        from SubmissionAuthor author
                        where author.submission = submission
                          and (
                                lower(author.fullName) like lower(concat('%', :query, '%'))
                                or lower(author.email) like lower(concat('%', :query, '%'))
                          )
                    )
              )
            order by submission.createdAt desc
            """)
    List<ArticleSubmission> search(
            @Param("status") ArticleStatus status,
            @Param("categoryId") Long categoryId,
            @Param("query") String query
    );

    long countByStatus(ArticleStatus status);
}

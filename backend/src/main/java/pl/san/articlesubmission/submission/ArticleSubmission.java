package pl.san.articlesubmission.submission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import pl.san.articlesubmission.common.BaseEntity;
import pl.san.articlesubmission.user.User;

@Entity
@Table(name = "article_submissions")
public class ArticleSubmission extends BaseEntity {

    @Column(name = "title", nullable = false, length = 300)
    private String title;

    @Column(name = "abstract_text", nullable = false, length = 5000)
    private String abstractText;

    @Column(name = "keywords", nullable = false, length = 1000)
    private String keywords;

    @Column(name = "corresponding_author_email", nullable = false, length = 180)
    private String correspondingAuthorEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private ArticleStatus status = ArticleStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ScientificCategory category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitted_by_id", nullable = false)
    private User submittedBy;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getCorrespondingAuthorEmail() {
        return correspondingAuthorEmail;
    }

    public void setCorrespondingAuthorEmail(String correspondingAuthorEmail) {
        this.correspondingAuthorEmail = correspondingAuthorEmail;
    }

    public ArticleStatus getStatus() {
        return status;
    }

    public void setStatus(ArticleStatus status) {
        this.status = status;
    }

    public ScientificCategory getCategory() {
        return category;
    }

    public void setCategory(ScientificCategory category) {
        this.category = category;
    }

    public User getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(User submittedBy) {
        this.submittedBy = submittedBy;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(OffsetDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}

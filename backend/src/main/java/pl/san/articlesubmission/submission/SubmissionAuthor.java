package pl.san.articlesubmission.submission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import pl.san.articlesubmission.common.BaseEntity;

@Entity
@Table(name = "submission_authors")
public class SubmissionAuthor extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private ArticleSubmission submission;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "email", nullable = false, length = 180)
    private String email;

    @Column(name = "affiliation", nullable = false, length = 255)
    private String affiliation;

    @Column(name = "author_order", nullable = false)
    private int authorOrder;

    @Column(name = "corresponding_author", nullable = false)
    private boolean correspondingAuthor;

    public ArticleSubmission getSubmission() {
        return submission;
    }

    public void setSubmission(ArticleSubmission submission) {
        this.submission = submission;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public int getAuthorOrder() {
        return authorOrder;
    }

    public void setAuthorOrder(int authorOrder) {
        this.authorOrder = authorOrder;
    }

    public boolean isCorrespondingAuthor() {
        return correspondingAuthor;
    }

    public void setCorrespondingAuthor(boolean correspondingAuthor) {
        this.correspondingAuthor = correspondingAuthor;
    }
}

package pl.san.articlesubmission.review;

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
import pl.san.articlesubmission.submission.ArticleSubmission;
import pl.san.articlesubmission.user.User;

@Entity
@Table(name = "review_assignments")
public class ReviewAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private ArticleSubmission submission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ReviewAssignmentStatus status = ReviewAssignmentStatus.ASSIGNED;

    @Column(name = "assigned_at", nullable = false)
    private OffsetDateTime assignedAt = OffsetDateTime.now();

    @Column(name = "due_date")
    private OffsetDateTime dueDate;

    public ArticleSubmission getSubmission() {
        return submission;
    }

    public void setSubmission(ArticleSubmission submission) {
        this.submission = submission;
    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public ReviewAssignmentStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewAssignmentStatus status) {
        this.status = status;
    }

    public OffsetDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(OffsetDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public OffsetDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(OffsetDateTime dueDate) {
        this.dueDate = dueDate;
    }
}

package pl.san.articlesubmission.review;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import pl.san.articlesubmission.common.BaseEntity;

@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false, unique = true)
    private ReviewAssignment assignment;

    @Column(name = "score", nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation", nullable = false, length = 40)
    private ReviewRecommendation recommendation;

    @Column(name = "summary_comment", nullable = false, length = 1000)
    private String summaryComment;

    @Column(name = "detailed_comment", nullable = false, length = 5000)
    private String detailedComment;

    @Column(name = "submitted_at", nullable = false)
    private OffsetDateTime submittedAt = OffsetDateTime.now();

    public ReviewAssignment getAssignment() {
        return assignment;
    }

    public void setAssignment(ReviewAssignment assignment) {
        this.assignment = assignment;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public ReviewRecommendation getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(ReviewRecommendation recommendation) {
        this.recommendation = recommendation;
    }

    public String getSummaryComment() {
        return summaryComment;
    }

    public void setSummaryComment(String summaryComment) {
        this.summaryComment = summaryComment;
    }

    public String getDetailedComment() {
        return detailedComment;
    }

    public void setDetailedComment(String detailedComment) {
        this.detailedComment = detailedComment;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(OffsetDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}

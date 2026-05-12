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
@Table(name = "editorial_decisions")
public class EditorialDecision extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private ArticleSubmission submission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "editor_id", nullable = false)
    private User editor;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_type", nullable = false, length = 20)
    private DecisionType decisionType;

    @Column(name = "decision_note", nullable = false, length = 3000)
    private String decisionNote;

    @Column(name = "decided_at", nullable = false)
    private OffsetDateTime decidedAt = OffsetDateTime.now();

    public ArticleSubmission getSubmission() {
        return submission;
    }

    public void setSubmission(ArticleSubmission submission) {
        this.submission = submission;
    }

    public User getEditor() {
        return editor;
    }

    public void setEditor(User editor) {
        this.editor = editor;
    }

    public DecisionType getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(DecisionType decisionType) {
        this.decisionType = decisionType;
    }

    public String getDecisionNote() {
        return decisionNote;
    }

    public void setDecisionNote(String decisionNote) {
        this.decisionNote = decisionNote;
    }

    public OffsetDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(OffsetDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }
}

package pl.san.articlesubmission.submission;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import pl.san.articlesubmission.common.BaseEntity;

@Entity
@Table(name = "submission_files")
public class SubmissionFile extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private ArticleSubmission submission;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false, length = 255)
    private String storedFileName;

    @Column(name = "media_type", nullable = false, length = 120)
    private String mediaType;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 30)
    private SubmissionFileType fileType;

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    public ArticleSubmission getSubmission() {
        return submission;
    }

    public void setSubmission(ArticleSubmission submission) {
        this.submission = submission;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public SubmissionFileType getFileType() {
        return fileType;
    }

    public void setFileType(SubmissionFileType fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}

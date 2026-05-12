package pl.san.articlesubmission.reporting.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.san.articlesubmission.review.ReviewAssignmentStatus;
import pl.san.articlesubmission.review.repository.ReviewAssignmentRepository;
import pl.san.articlesubmission.reporting.dto.ReportSummaryResponse;
import pl.san.articlesubmission.submission.ArticleStatus;
import pl.san.articlesubmission.submission.ArticleSubmission;
import pl.san.articlesubmission.submission.repository.ArticleSubmissionRepository;
import pl.san.articlesubmission.submission.repository.SubmissionFileRepository;

@Service
public class ReportingService {

    private static final DateTimeFormatter EXPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ArticleSubmissionRepository articleSubmissionRepository;
    private final ReviewAssignmentRepository reviewAssignmentRepository;
    private final SubmissionFileRepository submissionFileRepository;

    public ReportingService(
            ArticleSubmissionRepository articleSubmissionRepository,
            ReviewAssignmentRepository reviewAssignmentRepository,
            SubmissionFileRepository submissionFileRepository
    ) {
        this.articleSubmissionRepository = articleSubmissionRepository;
        this.reviewAssignmentRepository = reviewAssignmentRepository;
        this.submissionFileRepository = submissionFileRepository;
    }

    @Transactional(readOnly = true)
    public ReportSummaryResponse buildSummary() {
        return new ReportSummaryResponse(
                articleSubmissionRepository.count(),
                articleSubmissionRepository.countByStatus(ArticleStatus.DRAFT),
                articleSubmissionRepository.countByStatus(ArticleStatus.SUBMITTED),
                articleSubmissionRepository.countByStatus(ArticleStatus.IN_REVIEW),
                articleSubmissionRepository.countByStatus(ArticleStatus.REVIEW_COMPLETED),
                articleSubmissionRepository.countByStatus(ArticleStatus.ACCEPTED),
                articleSubmissionRepository.countByStatus(ArticleStatus.REJECTED),
                articleSubmissionRepository.countByStatus(ArticleStatus.PUBLISHED),
                reviewAssignmentRepository.count(),
                reviewAssignmentRepository.countByStatus(ReviewAssignmentStatus.SUBMITTED),
                submissionFileRepository.count()
        );
    }

    @Transactional(readOnly = true)
    public byte[] exportSubmissionsAsCsv() {
        StringBuilder builder = new StringBuilder();
        builder.append("ID,Title,Status,Category,Submitted By,Submitted At,Updated At").append(System.lineSeparator());

        for (ArticleSubmission submission : fetchSortedSubmissions()) {
            builder.append(submission.getId()).append(',')
                    .append(escapeCsv(submission.getTitle())).append(',')
                    .append(submission.getStatus().name()).append(',')
                    .append(escapeCsv(submission.getCategory().getName())).append(',')
                    .append(escapeCsv(submission.getSubmittedBy().getEmail())).append(',')
                    .append(escapeCsv(formatDateTime(submission.getSubmittedAt()))).append(',')
                    .append(escapeCsv(formatDateTime(submission.getUpdatedAt())))
                    .append(System.lineSeparator());
        }

        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional(readOnly = true)
    public byte[] exportSubmissionsAsPdf() {
        List<ArticleSubmission> submissions = fetchSortedSubmissions();
        ReportSummaryResponse summary = buildSummary();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            document.add(new Paragraph(
                    "Scientific Article Submission Report",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)
            ));
            document.add(new Paragraph("Generated at: " + formatDateTime(OffsetDateTime.now())));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(
                    "Summary: submissions=%d, published=%d, files=%d, submitted reviews=%d"
                            .formatted(
                                    summary.totalSubmissions(),
                                    summary.publishedSubmissions(),
                                    summary.totalFiles(),
                                    summary.submittedReviewAssignments()
                            )
            ));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            addHeaderCell(table, "Title");
            addHeaderCell(table, "Status");
            addHeaderCell(table, "Category");
            addHeaderCell(table, "Submitted By");
            addHeaderCell(table, "Submitted At");

            for (ArticleSubmission submission : submissions) {
                table.addCell(submission.getTitle());
                table.addCell(submission.getStatus().name());
                table.addCell(submission.getCategory().getName());
                table.addCell(submission.getSubmittedBy().getEmail());
                table.addCell(formatDateTime(submission.getSubmittedAt()));
            }

            document.add(table);
            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException exception) {
            throw new IllegalStateException("Failed to generate PDF report", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Unexpected error during PDF report generation", exception);
        }
    }

    private List<ArticleSubmission> fetchSortedSubmissions() {
        return articleSubmissionRepository.findAll().stream()
                .sorted(Comparator.comparing(ArticleSubmission::getCreatedAt).reversed())
                .toList();
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        table.addCell(cell);
    }

    private String formatDateTime(OffsetDateTime value) {
        return value == null ? "" : EXPORT_DATE_FORMAT.format(value);
    }

    private String escapeCsv(String value) {
        String normalized = value == null ? "" : value;
        return "\"" + normalized.replace("\"", "\"\"") + "\"";
    }
}

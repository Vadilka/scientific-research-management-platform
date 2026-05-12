package pl.san.articlesubmission.reporting.web;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.san.articlesubmission.reporting.dto.ReportSummaryResponse;
import pl.san.articlesubmission.reporting.service.ReportingService;

@RestController
@RequestMapping("/api/reports")
public class ReportingController {

    private static final DateTimeFormatter FILE_NAME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @GetMapping("/summary")
    public ReportSummaryResponse summary() {
        return reportingService.buildSummary();
    }

    @GetMapping("/submissions/export/csv")
    public ResponseEntity<byte[]> exportSubmissionsCsv() {
        byte[] content = reportingService.exportSubmissionsAsCsv();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(buildFileName("submissions-report", "csv"))
                        .build()
                        .toString())
                .body(content);
    }

    @GetMapping("/submissions/export/pdf")
    public ResponseEntity<byte[]> exportSubmissionsPdf() {
        byte[] content = reportingService.exportSubmissionsAsPdf();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(buildFileName("submissions-report", "pdf"))
                        .build()
                        .toString())
                .body(content);
    }

    private String buildFileName(String prefix, String extension) {
        return prefix + "-" + FILE_NAME_FORMAT.format(OffsetDateTime.now()) + "." + extension;
    }
}

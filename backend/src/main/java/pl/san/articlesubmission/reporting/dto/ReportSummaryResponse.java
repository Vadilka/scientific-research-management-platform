package pl.san.articlesubmission.reporting.dto;

public record ReportSummaryResponse(
        long totalSubmissions,
        long draftSubmissions,
        long submittedSubmissions,
        long inReviewSubmissions,
        long reviewCompletedSubmissions,
        long acceptedSubmissions,
        long rejectedSubmissions,
        long publishedSubmissions,
        long totalReviewAssignments,
        long submittedReviewAssignments,
        long totalFiles
) {
}

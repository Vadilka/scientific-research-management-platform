package pl.san.articlesubmission.submission.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.san.articlesubmission.submission.ArticleStatus;
import pl.san.articlesubmission.submission.dto.CreateSubmissionRequest;
import pl.san.articlesubmission.submission.dto.SubmissionDetailResponse;
import pl.san.articlesubmission.submission.dto.SubmissionSummaryResponse;
import pl.san.articlesubmission.submission.dto.UpdateSubmissionRequest;
import pl.san.articlesubmission.submission.service.SubmissionService;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @GetMapping
    public List<SubmissionSummaryResponse> findAll(
            @RequestParam(required = false) ArticleStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String query
    ) {
        return submissionService.findAll(status, categoryId, query);
    }

    @GetMapping("/{submissionId}")
    public SubmissionDetailResponse findById(@PathVariable Long submissionId) {
        return submissionService.findById(submissionId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionDetailResponse create(
            @Valid @RequestBody CreateSubmissionRequest request,
            Authentication authentication
    ) {
        return submissionService.create(request, authentication);
    }

    @PutMapping("/{submissionId}")
    public SubmissionDetailResponse updateDraft(
            @PathVariable Long submissionId,
            @Valid @RequestBody UpdateSubmissionRequest request,
            Authentication authentication
    ) {
        return submissionService.updateDraft(submissionId, request, authentication);
    }
}

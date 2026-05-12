package pl.san.articlesubmission.review.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.san.articlesubmission.review.dto.CreateEditorialDecisionRequest;
import pl.san.articlesubmission.review.dto.EditorialDecisionResponse;
import pl.san.articlesubmission.review.service.EditorialDecisionService;

@RestController
@RequestMapping("/api/editorial-decisions")
public class EditorialDecisionController {

    private final EditorialDecisionService editorialDecisionService;

    public EditorialDecisionController(EditorialDecisionService editorialDecisionService) {
        this.editorialDecisionService = editorialDecisionService;
    }

    @GetMapping("/submission/{submissionId}")
    public List<EditorialDecisionResponse> findBySubmissionId(@PathVariable Long submissionId) {
        return editorialDecisionService.findBySubmissionId(submissionId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EditorialDecisionResponse createDecision(
            @Valid @RequestBody CreateEditorialDecisionRequest request,
            Authentication authentication
    ) {
        return editorialDecisionService.createDecision(request, authentication);
    }
}

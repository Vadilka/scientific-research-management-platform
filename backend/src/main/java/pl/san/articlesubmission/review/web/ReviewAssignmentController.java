package pl.san.articlesubmission.review.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import pl.san.articlesubmission.review.dto.AssignReviewerRequest;
import pl.san.articlesubmission.review.dto.ReviewAssignmentResponse;
import pl.san.articlesubmission.review.service.ReviewAssignmentService;

@RestController
@RequestMapping("/api/review-assignments")
public class ReviewAssignmentController {

    private final ReviewAssignmentService reviewAssignmentService;

    public ReviewAssignmentController(ReviewAssignmentService reviewAssignmentService) {
        this.reviewAssignmentService = reviewAssignmentService;
    }

    @GetMapping
    public List<ReviewAssignmentResponse> findVisibleAssignments(Authentication authentication) {
        return reviewAssignmentService.findVisibleAssignments(authentication);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewAssignmentResponse assignReviewer(@Valid @RequestBody AssignReviewerRequest request) {
        return reviewAssignmentService.assignReviewer(request);
    }
}

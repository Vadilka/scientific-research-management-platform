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
import pl.san.articlesubmission.review.dto.CreateReviewRequest;
import pl.san.articlesubmission.review.dto.ReviewResponse;
import pl.san.articlesubmission.review.service.ReviewService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/submission/{submissionId}")
    public List<ReviewResponse> findBySubmissionId(@PathVariable Long submissionId) {
        return reviewService.findBySubmissionId(submissionId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse submitReview(
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication
    ) {
        return reviewService.submitReview(request, authentication);
    }
}

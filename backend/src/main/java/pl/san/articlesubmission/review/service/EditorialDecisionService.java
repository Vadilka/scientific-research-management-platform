package pl.san.articlesubmission.review.service;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.san.articlesubmission.common.web.BusinessRuleViolationException;
import pl.san.articlesubmission.common.web.ResourceNotFoundException;
import pl.san.articlesubmission.notification.NotificationType;
import pl.san.articlesubmission.notification.service.NotificationService;
import pl.san.articlesubmission.review.DecisionType;
import pl.san.articlesubmission.review.EditorialDecision;
import pl.san.articlesubmission.review.dto.CreateEditorialDecisionRequest;
import pl.san.articlesubmission.review.dto.EditorialDecisionResponse;
import pl.san.articlesubmission.review.repository.EditorialDecisionRepository;
import pl.san.articlesubmission.submission.ArticleStatus;
import pl.san.articlesubmission.submission.ArticleSubmission;
import pl.san.articlesubmission.submission.repository.ArticleSubmissionRepository;
import pl.san.articlesubmission.user.User;
import pl.san.articlesubmission.user.repository.UserRepository;

@Service
public class EditorialDecisionService {

    private final EditorialDecisionRepository editorialDecisionRepository;
    private final ArticleSubmissionRepository articleSubmissionRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;
    private final NotificationService notificationService;

    public EditorialDecisionService(
            EditorialDecisionRepository editorialDecisionRepository,
            ArticleSubmissionRepository articleSubmissionRepository,
            UserRepository userRepository,
            ReviewMapper reviewMapper,
            NotificationService notificationService
    ) {
        this.editorialDecisionRepository = editorialDecisionRepository;
        this.articleSubmissionRepository = articleSubmissionRepository;
        this.userRepository = userRepository;
        this.reviewMapper = reviewMapper;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<EditorialDecisionResponse> findBySubmissionId(Long submissionId) {
        return editorialDecisionRepository.findBySubmissionIdOrderByDecidedAtDesc(submissionId).stream()
                .map(reviewMapper::toEditorialDecisionResponse)
                .toList();
    }

    @Transactional
    public EditorialDecisionResponse createDecision(
            CreateEditorialDecisionRequest request,
            Authentication authentication
    ) {
        ArticleSubmission submission = articleSubmissionRepository.findById(request.submissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        User editor = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated editor not found"));

        DecisionType decisionType = parseDecisionType(request.decisionType());
        validateTransition(submission, decisionType);

        EditorialDecision decision = new EditorialDecision();
        decision.setSubmission(submission);
        decision.setEditor(editor);
        decision.setDecisionType(decisionType);
        decision.setDecisionNote(request.decisionNote().trim());
        decision.setDecidedAt(OffsetDateTime.now());

        submission.setStatus(resolveNextStatus(decisionType));

        EditorialDecision savedDecision = editorialDecisionRepository.save(decision);
        notificationService.notifyUser(
                submission.getSubmittedBy(),
                submission,
                decisionType == DecisionType.PUBLISH ? NotificationType.PUBLICATION : NotificationType.EDITORIAL_DECISION,
                resolveNotificationTitle(decisionType),
                resolveNotificationMessage(submission, decisionType)
        );
        return reviewMapper.toEditorialDecisionResponse(savedDecision);
    }

    private DecisionType parseDecisionType(String rawDecisionType) {
        try {
            return DecisionType.valueOf(rawDecisionType.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessRuleViolationException("Invalid editorial decision type");
        }
    }

    private void validateTransition(ArticleSubmission submission, DecisionType decisionType) {
        ArticleStatus currentStatus = submission.getStatus();

        switch (decisionType) {
            case ACCEPT, REJECT -> {
                if (currentStatus != ArticleStatus.REVIEW_COMPLETED) {
                    throw new BusinessRuleViolationException(
                            "Editorial accept or reject decisions require a review-completed submission"
                    );
                }
            }
            case PUBLISH -> {
                if (currentStatus != ArticleStatus.ACCEPTED) {
                    throw new BusinessRuleViolationException("Only accepted submissions can be published");
                }
            }
            default -> throw new BusinessRuleViolationException("Unsupported editorial decision type");
        }
    }

    private ArticleStatus resolveNextStatus(DecisionType decisionType) {
        return switch (decisionType) {
            case ACCEPT -> ArticleStatus.ACCEPTED;
            case REJECT -> ArticleStatus.REJECTED;
            case PUBLISH -> ArticleStatus.PUBLISHED;
        };
    }

    private String resolveNotificationTitle(DecisionType decisionType) {
        return switch (decisionType) {
            case ACCEPT -> "Submission accepted";
            case REJECT -> "Submission rejected";
            case PUBLISH -> "Submission published";
        };
    }

    private String resolveNotificationMessage(ArticleSubmission submission, DecisionType decisionType) {
        return switch (decisionType) {
            case ACCEPT -> "Your submission \"" + submission.getTitle() + "\" has been accepted.";
            case REJECT -> "Your submission \"" + submission.getTitle() + "\" has been rejected.";
            case PUBLISH -> "Your submission \"" + submission.getTitle() + "\" has been published.";
        };
    }
}

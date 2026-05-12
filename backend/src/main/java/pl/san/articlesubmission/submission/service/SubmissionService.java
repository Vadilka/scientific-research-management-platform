package pl.san.articlesubmission.submission.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.san.articlesubmission.common.web.BusinessRuleViolationException;
import pl.san.articlesubmission.common.web.ResourceNotFoundException;
import pl.san.articlesubmission.submission.ArticleStatus;
import pl.san.articlesubmission.submission.ArticleSubmission;
import pl.san.articlesubmission.submission.ScientificCategory;
import pl.san.articlesubmission.submission.SubmissionAuthor;
import pl.san.articlesubmission.submission.SubmissionFile;
import pl.san.articlesubmission.submission.dto.CreateSubmissionRequest;
import pl.san.articlesubmission.submission.dto.SubmissionDetailResponse;
import pl.san.articlesubmission.submission.dto.SubmissionSummaryResponse;
import pl.san.articlesubmission.submission.dto.UpdateSubmissionRequest;
import pl.san.articlesubmission.submission.repository.ArticleSubmissionRepository;
import pl.san.articlesubmission.submission.repository.ScientificCategoryRepository;
import pl.san.articlesubmission.submission.repository.SubmissionAuthorRepository;
import pl.san.articlesubmission.submission.repository.SubmissionFileRepository;
import pl.san.articlesubmission.user.User;
import pl.san.articlesubmission.user.repository.UserRepository;

@Service
public class SubmissionService {

    private final ArticleSubmissionRepository articleSubmissionRepository;
    private final ScientificCategoryRepository scientificCategoryRepository;
    private final SubmissionAuthorRepository submissionAuthorRepository;
    private final SubmissionFileRepository submissionFileRepository;
    private final UserRepository userRepository;
    private final SubmissionMapper submissionMapper;

    public SubmissionService(
            ArticleSubmissionRepository articleSubmissionRepository,
            ScientificCategoryRepository scientificCategoryRepository,
            SubmissionAuthorRepository submissionAuthorRepository,
            SubmissionFileRepository submissionFileRepository,
            UserRepository userRepository,
            SubmissionMapper submissionMapper
    ) {
        this.articleSubmissionRepository = articleSubmissionRepository;
        this.scientificCategoryRepository = scientificCategoryRepository;
        this.submissionAuthorRepository = submissionAuthorRepository;
        this.submissionFileRepository = submissionFileRepository;
        this.userRepository = userRepository;
        this.submissionMapper = submissionMapper;
    }

    @Transactional(readOnly = true)
    public List<SubmissionSummaryResponse> findAll(ArticleStatus status, Long categoryId, String query) {
        String normalizedQuery = normalizeQuery(query);
        List<ArticleSubmission> submissions = normalizedQuery == null
                ? articleSubmissionRepository.searchWithoutText(status, categoryId)
                : articleSubmissionRepository.search(status, categoryId, normalizedQuery);

        return submissions.stream()
                .map(submissionMapper::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubmissionDetailResponse findById(Long submissionId) {
        ArticleSubmission submission = findSubmission(submissionId);
        List<SubmissionAuthor> authors = submissionAuthorRepository
                .findBySubmissionIdOrderByAuthorOrderAsc(submissionId);
        List<SubmissionFile> files = submissionFileRepository.findBySubmissionIdOrderByCreatedAtDesc(submissionId);
        return submissionMapper.toDetailResponse(submission, authors, files);
    }

    @Transactional
    public SubmissionDetailResponse create(CreateSubmissionRequest request, Authentication authentication) {
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));

        ScientificCategory category = scientificCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Scientific category not found"));

        ArticleSubmission submission = new ArticleSubmission();
        submission.setTitle(request.title());
        submission.setAbstractText(request.abstractText());
        submission.setKeywords(submissionMapper.joinKeywords(request.keywords()));
        submission.setCorrespondingAuthorEmail(request.correspondingAuthorEmail());
        submission.setCategory(category);
        submission.setSubmittedBy(currentUser);

        if (request.submitNow()) {
            submission.setStatus(ArticleStatus.SUBMITTED);
            submission.setSubmittedAt(OffsetDateTime.now());
        } else {
            submission.setStatus(ArticleStatus.DRAFT);
        }

        ArticleSubmission savedSubmission = articleSubmissionRepository.save(submission);
        List<SubmissionAuthor> savedAuthors = submissionAuthorRepository.saveAll(
                request.authors().stream()
                        .map(authorRequest -> {
                            SubmissionAuthor author = new SubmissionAuthor();
                            author.setSubmission(savedSubmission);
                            author.setFullName(authorRequest.fullName());
                            author.setEmail(authorRequest.email());
                            author.setAffiliation(authorRequest.affiliation());
                            author.setAuthorOrder(authorRequest.authorOrder());
                            author.setCorrespondingAuthor(authorRequest.correspondingAuthor());
                            return author;
                        })
                        .toList()
        );

        return submissionMapper.toDetailResponse(savedSubmission, savedAuthors, List.of());
    }

    @Transactional
    public SubmissionDetailResponse updateDraft(
            Long submissionId,
            UpdateSubmissionRequest request,
            Authentication authentication
    ) {
        ArticleSubmission submission = findSubmission(submissionId);
        ensureDraftCanBeEdited(submission, authentication);

        ScientificCategory category = scientificCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Scientific category not found"));

        submission.setTitle(request.title());
        submission.setAbstractText(request.abstractText());
        submission.setKeywords(submissionMapper.joinKeywords(request.keywords()));
        submission.setCorrespondingAuthorEmail(request.correspondingAuthorEmail());
        submission.setCategory(category);

        if (request.submitNow()) {
            submission.setStatus(ArticleStatus.SUBMITTED);
            submission.setSubmittedAt(OffsetDateTime.now());
        }

        submissionAuthorRepository.deleteBySubmissionId(submissionId);
        ArticleSubmission savedSubmission = articleSubmissionRepository.save(submission);
        List<SubmissionAuthor> savedAuthors = saveAuthors(savedSubmission, request.authors());
        List<SubmissionFile> files = submissionFileRepository.findBySubmissionIdOrderByCreatedAtDesc(submissionId);

        return submissionMapper.toDetailResponse(savedSubmission, savedAuthors, files);
    }

    public ArticleSubmission findSubmission(Long submissionId) {
        return articleSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
    }

    private List<SubmissionAuthor> saveAuthors(
            ArticleSubmission savedSubmission,
            List<pl.san.articlesubmission.submission.dto.SubmissionAuthorRequest> authorRequests
    ) {
        return submissionAuthorRepository.saveAll(
                authorRequests.stream()
                        .map(authorRequest -> {
                            SubmissionAuthor author = new SubmissionAuthor();
                            author.setSubmission(savedSubmission);
                            author.setFullName(authorRequest.fullName());
                            author.setEmail(authorRequest.email());
                            author.setAffiliation(authorRequest.affiliation());
                            author.setAuthorOrder(authorRequest.authorOrder());
                            author.setCorrespondingAuthor(authorRequest.correspondingAuthor());
                            return author;
                        })
                        .toList()
        );
    }

    private void ensureDraftCanBeEdited(ArticleSubmission submission, Authentication authentication) {
        if (submission.getStatus() != ArticleStatus.DRAFT) {
            throw new BusinessRuleViolationException("Only draft submissions can be edited");
        }

        boolean ownsSubmission = submission.getSubmittedBy().getEmail().equalsIgnoreCase(authentication.getName());
        boolean hasEditorialRole = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_EDITOR") || authority.equals("ROLE_ADMIN"));

        if (!ownsSubmission && !hasEditorialRole) {
            throw new BusinessRuleViolationException("Only the owner, editor, or administrator can edit this draft");
        }
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }

        return query.trim().toLowerCase(Locale.ROOT);
    }
}

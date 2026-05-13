package pl.san.articlesubmission.submission.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pl.san.articlesubmission.common.web.BusinessRuleViolationException;
import pl.san.articlesubmission.submission.ArticleStatus;
import pl.san.articlesubmission.submission.ScientificCategory;
import pl.san.articlesubmission.submission.dto.CreateSubmissionRequest;
import pl.san.articlesubmission.submission.dto.SubmissionAuthorRequest;
import pl.san.articlesubmission.submission.dto.SubmissionDetailResponse;
import pl.san.articlesubmission.submission.dto.SubmissionSummaryResponse;
import pl.san.articlesubmission.submission.dto.UpdateSubmissionRequest;
import pl.san.articlesubmission.submission.repository.ScientificCategoryRepository;
import pl.san.articlesubmission.user.RoleName;
import pl.san.articlesubmission.user.User;
import pl.san.articlesubmission.user.repository.UserRepository;

@SpringBootTest
class SubmissionServiceIntegrationTest {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private ScientificCategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void searchesAndUpdatesDraftBeforeReviewSubmission() {
        User author = createAuthor("draft.author@san.local");
        ScientificCategory category = createCategory("TEST-CS", "Test Computer Science");
        Authentication authentication = authentication(author.getEmail(), "ROLE_AUTHOR");

        SubmissionDetailResponse draft = submissionService.create(new CreateSubmissionRequest(
                "Initial draft title",
                "Initial abstract",
                List.of("workflow", "draft"),
                author.getEmail(),
                category.getId(),
                List.of(authorRequest("Draft Author", author.getEmail())),
                false
        ), authentication);

        SubmissionDetailResponse updatedDraft = submissionService.updateDraft(draft.id(), new UpdateSubmissionRequest(
                "Updated quantum workflow article",
                "Updated abstract with searchable phrase",
                List.of("quantum", "workflow"),
                author.getEmail(),
                category.getId(),
                List.of(authorRequest("Updated Author", author.getEmail())),
                false
        ), authentication);

        assertThat(updatedDraft.status()).isEqualTo(ArticleStatus.DRAFT.name());
        assertThat(submissionService.findAll(null, null, "quantum workflow")).hasSize(1);
        assertThat(submissionService.findAll(ArticleStatus.DRAFT, category.getId(), "updated author")).hasSize(1);

        SubmissionDetailResponse submitted = submissionService.updateDraft(draft.id(), new UpdateSubmissionRequest(
                "Submitted workflow article",
                "Submitted abstract",
                List.of("submitted", "workflow"),
                author.getEmail(),
                category.getId(),
                List.of(authorRequest("Updated Author", author.getEmail())),
                true
        ), authentication);

        assertThat(submitted.status()).isEqualTo(ArticleStatus.SUBMITTED.name());
        assertThatThrownBy(() -> submissionService.updateDraft(draft.id(), new UpdateSubmissionRequest(
                "Illegal update",
                "Should not be accepted",
                List.of("blocked"),
                author.getEmail(),
                category.getId(),
                List.of(authorRequest("Updated Author", author.getEmail())),
                false
        ), authentication)).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void allowsSameTitleForDifferentAuthorsAndKeepsOwnershipSeparated() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        ScientificCategory category = createCategory("DUPE-" + suffix, "Duplicate Title " + suffix);
        User firstAuthor = createAuthor("first.author." + suffix + "@san.local");
        User secondAuthor = createAuthor("second.author." + suffix + "@san.local");
        String sharedTitle = "Shared research topic " + suffix;

        SubmissionDetailResponse firstSubmission = submissionService.create(new CreateSubmissionRequest(
                sharedTitle,
                "First author abstract",
                List.of("shared", "topic"),
                firstAuthor.getEmail(),
                category.getId(),
                List.of(authorRequest("First Author", firstAuthor.getEmail())),
                true
        ), authentication(firstAuthor.getEmail(), "ROLE_AUTHOR"));

        SubmissionDetailResponse secondSubmission = submissionService.create(new CreateSubmissionRequest(
                sharedTitle,
                "Second author abstract",
                List.of("shared", "topic"),
                secondAuthor.getEmail(),
                category.getId(),
                List.of(authorRequest("Second Author", secondAuthor.getEmail())),
                true
        ), authentication(secondAuthor.getEmail(), "ROLE_AUTHOR"));

        assertThat(firstSubmission.id()).isNotEqualTo(secondSubmission.id());
        assertThat(firstSubmission.submittedByEmail()).isEqualTo(firstAuthor.getEmail());
        assertThat(secondSubmission.submittedByEmail()).isEqualTo(secondAuthor.getEmail());
        assertThat(submissionService.findAll(ArticleStatus.SUBMITTED, category.getId(), sharedTitle))
                .extracting(SubmissionSummaryResponse::title)
                .containsExactlyInAnyOrder(sharedTitle, sharedTitle);
    }

    @Test
    void rejectsDraftEditByAnotherAuthor() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User owner = createAuthor("draft.owner." + suffix + "@san.local");
        User anotherAuthor = createAuthor("draft.intruder." + suffix + "@san.local");
        ScientificCategory category = createCategory("OWN-" + suffix, "Ownership " + suffix);

        SubmissionDetailResponse draft = submissionService.create(new CreateSubmissionRequest(
                "Private draft " + suffix,
                "Only the owner should be able to edit this draft.",
                List.of("ownership"),
                owner.getEmail(),
                category.getId(),
                List.of(authorRequest("Draft Owner", owner.getEmail())),
                false
        ), authentication(owner.getEmail(), "ROLE_AUTHOR"));

        assertThatThrownBy(() -> submissionService.updateDraft(draft.id(), new UpdateSubmissionRequest(
                "Hijacked draft",
                "This update must be rejected.",
                List.of("blocked"),
                anotherAuthor.getEmail(),
                category.getId(),
                List.of(authorRequest("Intruder", anotherAuthor.getEmail())),
                false
        ), authentication(anotherAuthor.getEmail(), "ROLE_AUTHOR")))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Only the owner, editor, or administrator can edit this draft");
    }

    private User createAuthor(String email) {
        User user = new User();
        user.setFullName("Draft Author");
        user.setEmail(email);
        user.setPasswordHash("{noop}password");
        user.setRoleName(RoleName.AUTHOR);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private ScientificCategory createCategory(String code, String name) {
        ScientificCategory category = new ScientificCategory();
        category.setCode(code);
        category.setName(name);
        category.setDescription("Test category");
        return categoryRepository.save(category);
    }

    private SubmissionAuthorRequest authorRequest(String fullName, String email) {
        return new SubmissionAuthorRequest(
                fullName,
                email,
                "Społeczna Akademia Nauk",
                0,
                true
        );
    }

    private Authentication authentication(String email, String role) {
        return new UsernamePasswordAuthenticationToken(
                email,
                "password",
                List.of(new SimpleGrantedAuthority(role))
        );
    }
}

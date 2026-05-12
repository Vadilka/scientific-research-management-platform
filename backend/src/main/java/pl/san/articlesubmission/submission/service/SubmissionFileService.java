package pl.san.articlesubmission.submission.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.san.articlesubmission.common.web.BusinessRuleViolationException;
import pl.san.articlesubmission.common.web.ResourceNotFoundException;
import pl.san.articlesubmission.submission.ArticleStatus;
import pl.san.articlesubmission.submission.ArticleSubmission;
import pl.san.articlesubmission.submission.SubmissionFile;
import pl.san.articlesubmission.submission.SubmissionFileType;
import pl.san.articlesubmission.submission.dto.SubmissionFileResponse;
import pl.san.articlesubmission.submission.repository.SubmissionFileRepository;

@Service
public class SubmissionFileService {

    private static final List<String> LATEX_EXTENSIONS = List.of("tex", "zip");
    private static final List<String> PDF_EXTENSIONS = List.of("pdf");
    private static final List<String> DOCX_EXTENSIONS = List.of("docx");

    private final SubmissionService submissionService;
    private final SubmissionFileRepository submissionFileRepository;
    private final Path storageDirectory;

    public SubmissionFileService(
            SubmissionService submissionService,
            SubmissionFileRepository submissionFileRepository,
            @Value("${app.storage.submission-files-dir:./data/submission-files}") String storageDirectory
    ) {
        this.submissionService = submissionService;
        this.submissionFileRepository = submissionFileRepository;
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public List<SubmissionFileResponse> findBySubmissionId(Long submissionId) {
        submissionService.findSubmission(submissionId);
        return submissionFileRepository.findBySubmissionIdOrderByCreatedAtDesc(submissionId).stream()
                .map(file -> new SubmissionFileResponse(
                        file.getId(),
                        file.getOriginalFileName(),
                        file.getMediaType(),
                        file.getFileType().name(),
                        file.getFileSize(),
                        file.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public SubmissionFileResponse uploadFile(
            Long submissionId,
            String rawFileType,
            MultipartFile multipartFile,
            Authentication authentication
    ) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BusinessRuleViolationException("Uploaded file cannot be empty");
        }

        ArticleSubmission submission = submissionService.findSubmission(submissionId);
        validateUploadPermission(submission, authentication);

        SubmissionFileType fileType = parseFileType(rawFileType);
        String originalFileName = sanitizeFileName(multipartFile.getOriginalFilename());
        validateFileExtension(fileType, originalFileName);

        ensureStorageDirectoryExists();

        String extension = extractExtension(originalFileName);
        String storedFileName = UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);
        Path targetFile = storageDirectory.resolve(storedFileName).normalize();

        if (!targetFile.startsWith(storageDirectory)) {
            throw new BusinessRuleViolationException("Invalid file storage location");
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new BusinessRuleViolationException("Failed to store the uploaded file");
        }

        SubmissionFile submissionFile = new SubmissionFile();
        submissionFile.setSubmission(submission);
        submissionFile.setOriginalFileName(originalFileName);
        submissionFile.setStoredFileName(storedFileName);
        submissionFile.setMediaType(resolveMediaType(multipartFile, fileType));
        submissionFile.setFileType(fileType);
        submissionFile.setFileSize(multipartFile.getSize());

        SubmissionFile savedFile = submissionFileRepository.save(submissionFile);
        return new SubmissionFileResponse(
                savedFile.getId(),
                savedFile.getOriginalFileName(),
                savedFile.getMediaType(),
                savedFile.getFileType().name(),
                savedFile.getFileSize(),
                savedFile.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadFile(Long submissionId, Long fileId, Authentication authentication) {
        ArticleSubmission submission = submissionService.findSubmission(submissionId);
        validateViewPermission(submission, authentication);

        SubmissionFile file = submissionFileRepository.findByIdAndSubmissionId(fileId, submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission file not found"));

        Path storedFile = storageDirectory.resolve(file.getStoredFileName()).normalize();
        if (!Files.exists(storedFile)) {
            throw new ResourceNotFoundException("Stored file content not found");
        }

        try {
            Resource resource = new UrlResource(storedFile.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.getMediaType()))
                    .contentLength(file.getFileSize())
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                            .filename(file.getOriginalFileName())
                            .build()
                            .toString())
                    .body(resource);
        } catch (IOException exception) {
            throw new BusinessRuleViolationException("Failed to load the requested file");
        }
    }

    private void validateUploadPermission(ArticleSubmission submission, Authentication authentication) {
        String userEmail = authentication.getName();
        boolean isPrivileged = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_EDITOR".equals(authority.getAuthority())
                        || "ROLE_ADMIN".equals(authority.getAuthority()));

        boolean isOwner = submission.getSubmittedBy().getEmail().equals(userEmail);
        if (!isPrivileged && !isOwner) {
            throw new BusinessRuleViolationException("You can upload files only for your own submissions");
        }

        if (submission.getStatus() != ArticleStatus.DRAFT && submission.getStatus() != ArticleStatus.SUBMITTED) {
            throw new BusinessRuleViolationException(
                    "Files can only be uploaded while the submission is still in draft or submitted state"
            );
        }
    }

    private void validateViewPermission(ArticleSubmission submission, Authentication authentication) {
        String userEmail = authentication.getName();
        boolean privileged = authentication.getAuthorities().stream()
                .anyMatch(authority -> List.of("ROLE_EDITOR", "ROLE_ADMIN")
                        .contains(authority.getAuthority()));
        if (!privileged && !submission.getSubmittedBy().getEmail().equals(userEmail)) {
            throw new BusinessRuleViolationException("You can only access files for your own submissions");
        }
    }

    private SubmissionFileType parseFileType(String rawFileType) {
        try {
            return SubmissionFileType.valueOf(rawFileType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BusinessRuleViolationException("Invalid submission file type");
        }
    }

    private void validateFileExtension(SubmissionFileType fileType, String fileName) {
        String extension = extractExtension(fileName);
        List<String> allowedExtensions = switch (fileType) {
            case PDF -> PDF_EXTENSIONS;
            case DOCX -> DOCX_EXTENSIONS;
            case LATEX_SOURCE -> LATEX_EXTENSIONS;
        };

        if (!allowedExtensions.contains(extension)) {
            throw new BusinessRuleViolationException("File extension does not match the selected file type");
        }
    }

    private String resolveMediaType(MultipartFile multipartFile, SubmissionFileType fileType) {
        String providedMediaType = multipartFile.getContentType();
        if (providedMediaType != null && !providedMediaType.isBlank()) {
            return providedMediaType;
        }

        return switch (fileType) {
            case PDF -> MediaType.APPLICATION_PDF_VALUE;
            case DOCX -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case LATEX_SOURCE -> "application/x-tex";
        };
    }

    private void ensureStorageDirectoryExists() {
        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException exception) {
            throw new BusinessRuleViolationException("Failed to initialize file storage directory");
        }
    }

    private String sanitizeFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new BusinessRuleViolationException("Original file name is missing");
        }

        return Path.of(originalFileName).getFileName().toString();
    }

    private String extractExtension(String fileName) {
        int separatorIndex = fileName.lastIndexOf('.');
        if (separatorIndex < 0 || separatorIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(separatorIndex + 1).toLowerCase(Locale.ROOT);
    }
}

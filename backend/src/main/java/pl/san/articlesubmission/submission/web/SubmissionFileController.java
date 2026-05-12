package pl.san.articlesubmission.submission.web;

import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.san.articlesubmission.submission.dto.SubmissionFileResponse;
import pl.san.articlesubmission.submission.service.SubmissionFileService;

@RestController
@RequestMapping("/api/submissions/{submissionId}/files")
public class SubmissionFileController {

    private final SubmissionFileService submissionFileService;

    public SubmissionFileController(SubmissionFileService submissionFileService) {
        this.submissionFileService = submissionFileService;
    }

    @GetMapping
    public List<SubmissionFileResponse> listFiles(@PathVariable Long submissionId) {
        return submissionFileService.findBySubmissionId(submissionId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionFileResponse uploadFile(
            @PathVariable Long submissionId,
            @RequestParam("fileType") String fileType,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        return submissionFileService.uploadFile(submissionId, fileType, file, authentication);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long submissionId,
            @PathVariable Long fileId,
            Authentication authentication
    ) {
        return submissionFileService.downloadFile(submissionId, fileId, authentication);
    }
}

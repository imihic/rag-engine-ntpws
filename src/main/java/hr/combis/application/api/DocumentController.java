package hr.combis.application.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
@RestController
@RequestMapping("/api/v1/documents")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DocumentController {

    private final DocumentProcessingService documentProcessingService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("user_id") Long userId) {
        try {
            log.debug("Received file upload request for file: {}", file.getOriginalFilename());
            // Pass the file and user ID to the service layer
            String message = documentProcessingService.processDocument(file, userId);
            // Return the result as a JSON response
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            log.error("Error while processing file upload: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error processing file upload.");
        }
    }
}

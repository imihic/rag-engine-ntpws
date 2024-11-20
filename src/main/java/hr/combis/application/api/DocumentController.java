package hr.combis.application.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
@RestController
@RequestMapping("/api/v1/documents")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)

public class DocumentController {

    private final DocumentProcessingService documentProcessingService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            // Pass the file to the service layer for processing and return result
            String processedChunks = documentProcessingService.processDocument(file);
            // Return the result as a JSON response
            return ResponseEntity.ok(processedChunks);
        } catch (Exception e) {
            log.error("Error while processing file upload: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error processing file upload.");
        }
    }
}

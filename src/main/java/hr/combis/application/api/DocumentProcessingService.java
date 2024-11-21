package hr.combis.application.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.segment.TextSegment;
import hr.combis.application.api.dto.SegmentData;
import hr.combis.application.data.model.DocumentSegment;
import hr.combis.application.data.model.User;
import hr.combis.application.pipelines.PipelineExecutor;
import hr.combis.application.pipelines.PipelineFactory;
import hr.combis.application.pipelines.jobs.DocumentProcessingJob;
import hr.combis.application.pipelines.util.models.BgeM3EmbeddingModel;
import hr.combis.application.security.AuthenticatedUser;
import hr.combis.application.services.DocumentSegmentService;
import hr.combis.application.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DocumentProcessingService {

    private final PipelineFactory pipelineFactory;
    private final DocumentSegmentService documentSegmentService;
    private final UserService userService; // Assuming UserService exists to fetch user by ID

    public String processDocument(MultipartFile file, Long userId) {
        try {
            log.debug("Starting document processing for file: {}", file.getOriginalFilename());

            // Convert the MultipartFile to a byte array
            byte[] fileBytes = file.getBytes();

            // Fetch the user using the user ID
            User user = userService.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            log.debug("Processing document for user: {}", user.getId());

            // Create a DocumentProcessingJob
            DocumentProcessingJob job = new DocumentProcessingJob(1L, fileBytes);

            // Get the pipeline executor for document processing
            PipelineExecutor<DocumentProcessingJob> executor = pipelineFactory.createPipelineExecutor("documentIngestionPipeline");

            // Execute the pipeline
            executor.execute(job);
            log.debug("Document processing completed for file: {}", file.getOriginalFilename());

            // Save segments to the database
            List<DocumentSegment> segments = job.getSegments().stream()
                    .map(segment -> convertToEntity(segment, user))
                    .collect(Collectors.toList());

            documentSegmentService.saveAll(segments);

            return "Document processed and segments saved successfully.";
        } catch (IOException e) {
            log.error("Error reading file: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error processing document: {}", e.getMessage(), e);
        }
        return null;
    }

    private DocumentSegment convertToEntity(TextSegment segment, User user) {
        DocumentSegment entity = new DocumentSegment();
        entity.setDocumentId(segment.metadata().get("document_id"));
        entity.setText(segment.text());
        entity.setMetadata(segment.metadata().asMap());
        entity.setUser(user);

        // Compute embedding and set it
        float[] embedding = BgeM3EmbeddingModel.getInstance()
                .embed(segment.text())
                .content()
                .vector();

        entity.setEmbedding(embedding);

        return entity;
    }
}

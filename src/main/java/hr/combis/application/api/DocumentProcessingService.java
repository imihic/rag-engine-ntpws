package hr.combis.application.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.segment.TextSegment;
import hr.combis.application.api.dto.SegmentData;
import hr.combis.application.pipelines.PipelineExecutor;
import hr.combis.application.pipelines.PipelineFactory;
import hr.combis.application.pipelines.jobs.DocumentProcessingJob;
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

    public String processDocument(MultipartFile file) {
        try {
            log.info("Starting document processing for file: {}", file.getOriginalFilename());

            // Convert the MultipartFile to a byte array
            byte[] fileBytes = file.getBytes();

            // Create a DocumentProcessingJob
            DocumentProcessingJob job = new DocumentProcessingJob(1L, fileBytes);

            // Get the pipeline executor for document processing
            PipelineExecutor<DocumentProcessingJob> executor = pipelineFactory.createPipelineExecutor("documentIngestionPipeline");

            // Execute the pipeline
            executor.execute(job);
            log.info("Document processing completed for file: {}", file.getOriginalFilename());

            // Log each segment's text
            for (TextSegment segment : job.getSegments()) {
                log.info("Segment: {}", segment.text());
            }

            // Collect segments' text into a list of strings
            // Extract the text from each TextSegment
            // Return the list of segment texts
            // Collect segments' text and metadata into a list of SegmentData objects
            List<SegmentData> segmentDataList = job.getSegments().stream()
                    .map(segment -> new SegmentData(segment.text(), segment.metadata().asMap()))
                    .collect(Collectors.toList());

// Convert the list to a JSON string
            String json = getSegmentsAsJson(segmentDataList);

// Log or return the JSON string
            log.info("Segments JSON: {}", json);
            return json;
        } catch (IOException e) {
            log.error("Error reading file: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error processing document: {}", e.getMessage(), e);
        }
        return null;
    }


    // Method to convert segment data to JSON
    public String getSegmentsAsJson(List<SegmentData> segmentDataList) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(segmentDataList);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert segments to JSON", e);
        }
    }

    // Modified code to collect and convert segments
    public String collectAndConvertSegments(DocumentProcessingJob job) {
        // Collect segments' text and metadata into a list of SegmentData objects
        List<SegmentData> segmentDataList = job.getSegments().stream()
                .map(segment -> new SegmentData(segment.text(), segment.metadata().asMap()))
                .collect(Collectors.toList());

        // Convert the list to a JSON string
        String json = getSegmentsAsJson(segmentDataList);

        // Log or return the JSON string
        log.info("Segments JSON: {}", json);
        return json;
    }
}

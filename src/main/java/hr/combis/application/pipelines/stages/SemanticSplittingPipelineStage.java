package hr.combis.application.pipelines.stages;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import hr.combis.application.pipelines.jobs.DocumentProcessingJob;
import hr.combis.application.pipelines.util.models.BgeM3EmbeddingModel;
import hr.combis.application.pipelines.util.splitters.SemanticDocumentSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("semanticSplittingPipelineStage")
public class SemanticSplittingPipelineStage extends AbstractPipelineStage<DocumentProcessingJob> {

    private final EmbeddingModel embeddingModel;
    private final double similarityThreshold;
    private final int windowSize;

    public SemanticSplittingPipelineStage() {
        super(DocumentProcessingJob.class);
        this.embeddingModel = BgeM3EmbeddingModel.getInstance();
        this.similarityThreshold = 0.8; // Adjust as needed
        this.windowSize = 3; // Adjust as needed
    }

    @Override
    public DocumentProcessingJob process(DocumentProcessingJob job) {

        // Retrieve the document content from the job
        String documentContent = job.getDocumentContent();

        // Create the Document object
        Document document = Document.from(documentContent);

        // Create the semantic splitter with sliding window
        DocumentSplitter semanticSplitter = new SemanticDocumentSplitter(embeddingModel, windowSize);

        // Split the document
        List<TextSegment> segments = semanticSplitter.split(document);

        // Add segments to the job
        for (TextSegment segment : segments) {
            job.addSegment(segment);
        }

        // Update the job status
        job.setStatus(1); // Define status codes as per your application logic

        return job;
    }
}

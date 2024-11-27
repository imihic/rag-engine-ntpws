package hr.combis.application.pipelines.stages;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import hr.combis.application.pipelines.jobs.DocumentProcessingJob;
import hr.combis.application.pipelines.util.models.BgeM3EmbeddingModel;
import hr.combis.application.pipelines.util.splitters.ChunkGrouper;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Component("chunkGroupingPipelineStage")
@Slf4j
public class ChunkGroupingPipelineStage extends AbstractPipelineStage<DocumentProcessingJob> {

    private final EmbeddingModel embeddingModel;
    private final int bufferSize;
    private final ChunkGrouper chunkGrouper;

    public ChunkGroupingPipelineStage() {
        super(DocumentProcessingJob.class);
        this.embeddingModel = BgeM3EmbeddingModel.getInstance();
        this.bufferSize = 3; // Adjust as needed
        this.chunkGrouper = new ChunkGrouper(embeddingModel, bufferSize);
    }

    @Override
    public DocumentProcessingJob process(DocumentProcessingJob job) {
        String documentId = job.getDocumentId();
        List<TextSegment> sentenceSegments = job.getSentences();

        if (sentenceSegments == null || sentenceSegments.isEmpty()) {
            throw new IllegalStateException("No sentences found in the job. Ensure that SentenceSplittingPipelineStage has been executed.");
        }

        // Group sentences into chunks
        List<TextSegment> chunkSegments = chunkGrouper.groupSentencesIntoChunks(sentenceSegments, documentId);

        // Add chunks to job
        job.addSegments(chunkSegments);

        // Update job status
        job.setStatus(3); // Define status codes as per your application logic

        log.debug("Document {} grouped into {} chunks.", documentId, chunkSegments.size());

        return job;
    }
}

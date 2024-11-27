package hr.combis.application.pipelines.stages;

import dev.langchain4j.data.segment.TextSegment;
import hr.combis.application.pipelines.jobs.DocumentProcessingJob;
import hr.combis.application.pipelines.util.models.SentenceDetectorFactory;
import hr.combis.application.pipelines.util.splitters.SentenceSplitter;
import opennlp.tools.sentdetect.SentenceDetectorME;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Component("sentenceSplittingPipelineStage")
@Slf4j
public class SentenceSplittingPipelineStage extends AbstractPipelineStage<DocumentProcessingJob> {

    private final SentenceDetectorME sentenceDetector;
    private final SentenceSplitter sentenceSplitter;

    public SentenceSplittingPipelineStage() {
        super(DocumentProcessingJob.class);
        this.sentenceDetector = SentenceDetectorFactory.getSentenceDetector();
        this.sentenceSplitter = new SentenceSplitter(sentenceDetector);
    }

    @Override
    public DocumentProcessingJob process(DocumentProcessingJob job) {
        String documentContent = job.getDocumentContent();
        String documentId = UUID.randomUUID().toString(); // Or obtain from metadata
        job.setDocumentId(documentId);

        // Split document into sentences
        List<TextSegment> sentenceSegments = sentenceSplitter.splitIntoSentences(documentContent, documentId);

        // Store sentences in the job
        job.addSentences(sentenceSegments);

        // Update job status
        job.setStatus(2); // Define status codes as per your application logic

        log.debug("Document {} split into {} sentences.", documentId, sentenceSegments.size());

        return job;
    }
}

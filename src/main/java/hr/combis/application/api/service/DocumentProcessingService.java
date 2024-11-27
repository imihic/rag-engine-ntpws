package hr.combis.application.api.service;

import dev.langchain4j.data.segment.TextSegment;
import hr.combis.application.data.model.Chunk;
import hr.combis.application.data.model.DocumentEntity;
import hr.combis.application.data.model.Sentence;
import hr.combis.application.data.model.User;
import hr.combis.application.data.repository.ChunkRepository;
import hr.combis.application.data.repository.DocumentRepository;
import hr.combis.application.data.repository.SentenceRepository;
import hr.combis.application.pipelines.PipelineExecutor;
import hr.combis.application.pipelines.PipelineFactory;
import hr.combis.application.pipelines.jobs.DocumentProcessingJob;
import hr.combis.application.pipelines.util.models.BgeM3EmbeddingModel;
import hr.combis.application.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DocumentProcessingService {

    private final PipelineFactory pipelineFactory;
    private final DocumentRepository documentRepository;
    private final ChunkRepository chunkRepository;
    private final SentenceRepository sentenceRepository;
    private final UserService userService;

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

            // Save the Document entity
            DocumentEntity document = new DocumentEntity();
            document.setDocumentId(UUID.randomUUID().toString());
            document.setContent(job.getDocumentContent());
            document.setUser(user);
            documentRepository.save(document);

            // Save sentences
            List<Sentence> sentenceEntities = job.getSentences().stream()
                    .map(segment -> convertToSentenceEntity(segment, document))
                    .collect(Collectors.toList());
            sentenceRepository.saveAll(sentenceEntities);

            // Save chunks
            List<Chunk> chunkEntities = job.getSegments().stream()
                    .map(segment -> convertToChunkEntity(segment, document, sentenceEntities))
                    .collect(Collectors.toList());
            chunkRepository.saveAll(chunkEntities);

            return "Document processed and saved successfully.";
        } catch (IOException e) {
            log.error("Error reading file: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error processing document: {}", e.getMessage(), e);
        }
        return null;
    }

    private Sentence convertToSentenceEntity(TextSegment segment, DocumentEntity document) {
        Sentence sentence = new Sentence();
        sentence.setText(segment.text());
        sentence.setDocument(document);
        sentence.setSentenceIndex(Integer.parseInt(segment.metadata().getString("sentence_index")));

        // Compute embedding and set it
        float[] embedding = BgeM3EmbeddingModel.getInstance()
                .embed(segment.text())
                .content()
                .vector();

        sentence.setEmbedding(embedding);
        return sentence;
    }

    private Chunk convertToChunkEntity(TextSegment segment, DocumentEntity document, List<Sentence> sentenceEntities) {
        Chunk chunk = new Chunk();
        chunk.setText(segment.text());
        chunk.setDocument(document);

        // Compute embedding and set it
        float[] embedding = BgeM3EmbeddingModel.getInstance()
                .embed(segment.text())
                .content()
                .vector();

        chunk.setEmbedding(embedding);

        // Associate sentences with this chunk
        int chunkStartIndex = Integer.parseInt(segment.metadata().getString("chunk_start_index"));
        int chunkEndIndex = Integer.parseInt(segment.metadata().getString("chunk_end_index"));

        List<Sentence> sentencesInChunk = sentenceEntities.subList(chunkStartIndex, chunkEndIndex + 1);
        for (Sentence sentence : sentencesInChunk) {
            sentence.setChunk(chunk);
        }

        chunk.setSentences(sentencesInChunk);
        return chunk;
    }
}

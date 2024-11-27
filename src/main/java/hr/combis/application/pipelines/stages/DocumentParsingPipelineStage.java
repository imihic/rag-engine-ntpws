package hr.combis.application.pipelines.stages;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import hr.combis.application.pipelines.jobs.DocumentProcessingJob;
import hr.combis.application.pipelines.util.parsers.ApacheTikaDocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Component("documentParsingPipelineStage")
@Slf4j
public class DocumentParsingPipelineStage extends AbstractPipelineStage<DocumentProcessingJob> {


    public DocumentParsingPipelineStage() {
        super(DocumentProcessingJob.class);
    }

    @Override
    public DocumentProcessingJob process(DocumentProcessingJob job) {

        // Extract the text content from the file bytes using Apache Tika
        DocumentParser parser = new ApacheTikaDocumentParser();
        // Convert the byte array to an InputStream
        InputStream inputStream = new ByteArrayInputStream(job.getFileBytes());
        Document document = parser.parse(inputStream);
        job.setDocumentContent(document.text()); // Save the extracted content into the job
        // Update the job status
        job.setStatus(1); // Define status codes as per your application logic
        return job;
    }
}

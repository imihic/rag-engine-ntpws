package hr.combis.application.pipelines.jobs;

import dev.langchain4j.data.segment.TextSegment;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DocumentProcessingJob implements ProcessingJob {

    private Long id;
    private int status;
    private String documentContent;
    private byte[] fileBytes; // Field to store file bytes
    private List<TextSegment> segments = new ArrayList<>();

    public DocumentProcessingJob(Long id, byte[] fileBytes) {
        this.id = id;
        this.fileBytes = fileBytes;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    public void addSegment(TextSegment segment) {
        this.segments.add(segment);
    }

    public void addSegments(List<TextSegment> segments) {
        this.segments.addAll(segments);
    }
}

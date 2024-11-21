package hr.combis.application.services;

import hr.combis.application.data.model.DocumentSegment;
import hr.combis.application.data.model.User;
import hr.combis.application.data.repository.DocumentSegmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentSegmentService {

    private final DocumentSegmentRepository repository;

    @Autowired
    public DocumentSegmentService(DocumentSegmentRepository repository) {
        this.repository = repository;
    }

    public void saveAll(List<DocumentSegment> segments) {
        repository.saveAll(segments);
    }

    public void save(DocumentSegment segment) {
        repository.save(segment);
    }

    // Method to find the most similar segments for a given query embedding
    @Transactional
    public List<DocumentSegment> findSimilarSegments(User user, float[] queryEmbedding, int maxResults) {
        return repository.findSimilarSegments(user, queryEmbedding, maxResults);
    }
}

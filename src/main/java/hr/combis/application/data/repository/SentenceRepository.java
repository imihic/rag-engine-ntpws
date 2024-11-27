package hr.combis.application.data.repository;

import hr.combis.application.data.model.Chunk;
import hr.combis.application.data.model.DocumentEntity;
import hr.combis.application.data.model.Sentence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SentenceRepository extends JpaRepository<Sentence, Long> {
    List<Sentence> findByDocument(DocumentEntity document);
    List<Sentence> findByChunk(Chunk chunk);

}

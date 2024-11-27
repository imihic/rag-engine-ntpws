package hr.combis.application.data.repository;

import hr.combis.application.data.model.Chunk;
import hr.combis.application.data.model.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChunkRepository extends JpaRepository<Chunk, Long> {
    List<Chunk> findByDocument(DocumentEntity document);

}


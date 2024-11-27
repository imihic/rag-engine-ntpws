package hr.combis.application.data.repository;

import hr.combis.application.data.model.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
    Optional<DocumentEntity> findByDocumentId(String documentId);
}

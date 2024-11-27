package hr.combis.application.services;

import hr.combis.application.data.model.Chunk;
import hr.combis.application.data.model.User;
import hr.combis.application.data.repository.ChunkRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ChunkService {

    private final ChunkRepository chunkRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public ChunkService(ChunkRepository chunkRepository) {
        this.chunkRepository = chunkRepository;
    }

    public void saveAll(List<Chunk> chunks) {
        chunkRepository.saveAll(chunks);
    }

    @Transactional
    public List<Chunk> findSimilarChunks(User user, float[] queryEmbedding, int maxResults) {
        try {
            String hql = "SELECT c FROM Chunk c " +
                    "WHERE c.document.user = :user " +
                    "ORDER BY cosine_distance(c.embedding, :embedding) ASC";

            TypedQuery<Chunk> query = entityManager.createQuery(hql, Chunk.class);
            query.setParameter("user", user);
            query.setParameter("embedding", queryEmbedding);
            query.setMaxResults(maxResults);

            return query.getResultList();
        } catch (Exception e) {
            log.error("Error finding similar chunks for user {}: {}", user.getId(), e.getMessage());
            throw new RuntimeException("Failed to find similar chunks", e);
        }
    }
}

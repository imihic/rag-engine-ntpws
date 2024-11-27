package hr.combis.application.services;

import hr.combis.application.data.model.Sentence;
import hr.combis.application.data.model.User;
import hr.combis.application.data.repository.SentenceRepository;
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
public class SentenceService {

    private final SentenceRepository sentenceRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public SentenceService(SentenceRepository sentenceRepository) {
        this.sentenceRepository = sentenceRepository;
    }

    public void saveAll(List<Sentence> sentences) {
        sentenceRepository.saveAll(sentences);
    }

    @Transactional
    public List<Sentence> findSimilarSentences(User user, float[] queryEmbedding, int maxResults) {
        try {
            String hql = "SELECT s FROM Sentence s " +
                    "WHERE s.document.user = :user " +
                    "ORDER BY cosine_distance(s.embedding, :embedding) ASC";

            TypedQuery<Sentence> query = entityManager.createQuery(hql, Sentence.class);
            query.setParameter("user", user);
            query.setParameter("embedding", queryEmbedding);
            query.setMaxResults(maxResults);

            return query.getResultList();
        } catch (Exception e) {
            log.error("Error finding similar sentences for user {}: {}", user.getId(), e.getMessage());
            throw new RuntimeException("Failed to find similar sentences", e);
        }
    }
}

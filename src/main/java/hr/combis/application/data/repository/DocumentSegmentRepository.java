package hr.combis.application.data.repository;

import hr.combis.application.data.model.DocumentSegment;
import hr.combis.application.data.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface DocumentSegmentRepository extends JpaRepository<DocumentSegment, Long> {

    @Query(value = "SELECT ds FROM DocumentSegment ds WHERE ds.user = :user ORDER BY cosine_distance(ds.embedding, :embedding) ASC LIMIT :maxResults")
    List<DocumentSegment> findSimilarSegments(@Param("user") User user, @Param("embedding") float[] embedding, int maxResults);
}

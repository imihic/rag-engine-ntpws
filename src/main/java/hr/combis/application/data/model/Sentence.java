package hr.combis.application.data.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "sentences")
@Getter
@Setter
public class Sentence extends AbstractEntity {

    @Lob
    private String text;

    // Embedding for the sentence
    @Column(name = "embedding")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1024) // Adjust according to embedding size
    private float[] embedding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chunk_id")
    private Chunk chunk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private DocumentEntity document;

    private int sentenceIndex;

}
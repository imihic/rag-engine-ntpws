package hr.combis.application.data.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chunks")
@Getter
@Setter
public class Chunk extends AbstractEntity {

    @Lob
    private String text;

    // Embedding for the chunk
    @Column(name = "embedding")
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1024) // Adjust according to embedding size
    private float[] embedding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private DocumentEntity document;

    @OneToMany(mappedBy = "chunk", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sentence> sentences = new ArrayList<>();

    // Getters and setters
}


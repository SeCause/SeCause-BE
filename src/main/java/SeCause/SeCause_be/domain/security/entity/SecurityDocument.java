package SeCause.SeCause_be.domain.security.entity;

import SeCause.SeCause_be.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "security_documents")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SecurityDocument extends BaseEntity {

    private static final int EMBEDDING_DIMENSION = 1536;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "security_document_id")
    private Long securityDocumentId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "source_type", nullable = false, columnDefinition = "reference_type_enum")
    private ReferenceType sourceType;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "url", length = 1000)
    private String url;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private float[] embedding;

    private SecurityDocument(ReferenceType sourceType, String title, String url, String content, float[] embedding) {
        this.sourceType = sourceType;
        this.title = title;
        this.url = url;
        this.content = content;
        validateEmbedding(embedding);
        this.embedding = embedding;
    }

    public static SecurityDocument create(
            ReferenceType sourceType,
            String title,
            String url,
            String content,
            float[] embedding
    ) {
        return new SecurityDocument(sourceType, title, url, content, embedding);
    }

    private void validateEmbedding(float[] embedding) {
        if (embedding != null && embedding.length != EMBEDDING_DIMENSION) {
            throw new IllegalArgumentException("embedding dimension must be " + EMBEDDING_DIMENSION);
        }
    }
}

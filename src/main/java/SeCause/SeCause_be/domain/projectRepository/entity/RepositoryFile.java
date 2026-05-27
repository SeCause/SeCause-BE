package SeCause.SeCause_be.domain.projectRepository.entity;

import SeCause.SeCause_be.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "repository_files")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepositoryFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "repository_file_id")
    private Long repositoryFileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private ProjectRepository repository;

    @Column(name = "file_path", nullable = false, length = 1000)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "file_type", nullable = false, columnDefinition = "file_type_enum")
    private FileType fileType;

    @Column(name = "language", length = 50)
    private String language;

    @Column(name = "file_size_bytes", nullable = false)
    private long fileSizeBytes = 0L;

    private RepositoryFile(
            ProjectRepository repository,
            String filePath,
            FileType fileType,
            String language,
            long fileSizeBytes
    ) {
        this.repository = repository;
        this.filePath = filePath;
        this.fileType = fileType;
        this.language = language;
        this.fileSizeBytes = fileSizeBytes;
    }

    public static RepositoryFile create(
            ProjectRepository repository,
            String filePath,
            FileType fileType,
            String language,
            long fileSizeBytes
    ) {
        return new RepositoryFile(repository, filePath, fileType, language, fileSizeBytes);
    }
}

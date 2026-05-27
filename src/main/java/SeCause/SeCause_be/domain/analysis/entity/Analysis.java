package SeCause.SeCause_be.domain.analysis.entity;

import SeCause.SeCause_be.domain.projectRepository.entity.ProjectRepository;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "analyses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Analysis extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_id")
    private Long analysisId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false, unique = true)
    private ProjectRepository repository;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "analysis_status", nullable = false, columnDefinition = "analysis_status_enum")
    private AnalysisStatus analysisStatus = AnalysisStatus.PENDING;

    @Column(name = "progress_percent", nullable = false)
    private int progressPercent = 0;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    private Analysis(ProjectRepository repository) {
        this.repository = repository;
    }

    public static Analysis create(ProjectRepository repository) {
        return new Analysis(repository);
    }

    public void updateProgress(AnalysisStatus analysisStatus, int progressPercent) {
        this.analysisStatus = analysisStatus;
        this.progressPercent = progressPercent;
    }

    public void complete() {
        this.analysisStatus = AnalysisStatus.COMPLETED;
        this.progressPercent = 100;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String failureReason) {
        this.analysisStatus = AnalysisStatus.FAILED;
        this.failureReason = failureReason;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.analysisStatus = AnalysisStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
    }
}

package SeCause.SeCause_be.domain.analysis.entity;

import SeCause.SeCause_be.domain.vulnerability.entity.CodeVulnerability;
import SeCause.SeCause_be.domain.vulnerability.entity.InfraVulnerability;
import SeCause.SeCause_be.domain.vulnerability.entity.Vulnerability;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Getter
@Entity
@Table(name = "analysis_results")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "analysis_result_id")
    private Long analysisResultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vulnerability_id", nullable = false)
    private Vulnerability vulnerability;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "attack_scenario", columnDefinition = "TEXT")
    private String attackScenario;

    @Column(name = "fix_code", columnDefinition = "TEXT")
    private String fixCode;

    @Column(name = "fix_summary", columnDefinition = "TEXT")
    private String fixSummary;

    private AnalysisResult(
            Vulnerability vulnerability,
            String description,
            String summary,
            String attackScenario,
            String fixCode,
            String fixSummary
    ) {
        this.vulnerability = vulnerability;
        this.description = description;
        this.summary = summary;
        this.attackScenario = attackScenario;
        this.fixCode = fixCode;
        this.fixSummary = fixSummary;
    }

    public static AnalysisResult createForCodeVulnerability(
            CodeVulnerability codeVulnerability,
            String description,
            String summary,
            String attackScenario,
            String fixCode,
            String fixSummary
    ) {
        return new AnalysisResult(codeVulnerability, description, summary, attackScenario, fixCode, fixSummary);
    }

    public static AnalysisResult createForInfraVulnerability(
            InfraVulnerability infraVulnerability,
            String description,
            String summary,
            String attackScenario,
            String fixCode,
            String fixSummary
    ) {
        return new AnalysisResult(infraVulnerability, description, summary, attackScenario, fixCode, fixSummary);
    }
}

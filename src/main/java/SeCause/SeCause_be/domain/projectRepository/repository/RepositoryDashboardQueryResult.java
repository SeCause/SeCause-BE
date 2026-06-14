package SeCause.SeCause_be.domain.projectRepository.repository;

import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import SeCause.SeCause_be.domain.vulnerability.entity.Severity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record RepositoryDashboardQueryResult(
        Long repositoryId,
        String owner,
        String name,
        String description,
        String githubUrl,
        String branch,
        int fileCount,
        long lineCount,
        List<String> languages,
        AnalysisStatus analysisStatus,
        int progressPercent,
        LocalDateTime analysisRequestedAt,
        LocalDateTime completedAt,
        String failureReason,
        List<IssueTypeCount> issuesByType,
        Map<Severity, Long> issueCountsBySeverity
) {

    public record IssueTypeCount(
            String type,
            Severity severity,
            long count
    ) {
    }
}

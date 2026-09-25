package SeCause.SeCause_be.domain.projectRepository.dto;

import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;

import java.time.LocalDateTime;
import java.util.List;

public record RepositorySummaryResponse(
        Long repositoryId,
        String owner,
        String name,
        String fullName,
        String branch,
        int fileCount,
        long lineCount,
        List<String> languages,
        RepositorySeverityCountResponse issueCounts,
        AnalysisStatus analysisStatus,
        int progressPercent,
        LocalDateTime analysisRequestedAt,
        LocalDateTime completedAt
) {

    public static RepositorySummaryResponse of(
            Long repositoryId,
            String owner,
            String name,
            String branch,
            int fileCount,
            long lineCount,
            List<String> languages,
            RepositorySeverityCountResponse issueCounts,
            AnalysisStatus analysisStatus,
            int progressPercent,
            LocalDateTime analysisRequestedAt,
            LocalDateTime completedAt
    ) {
        String fullName = createFullName(owner, name);

        return new RepositorySummaryResponse(
                repositoryId,
                owner,
                name,
                fullName,
                branch,
                fileCount,
                lineCount,
                languages,
                issueCounts,
                analysisStatus,
                progressPercent,
                analysisRequestedAt,
                completedAt
        );
    }

    private static String createFullName(String owner, String name) {
        if (owner == null || owner.isBlank()) {
            return name == null ? "" : name;
        }
        if (name == null || name.isBlank()) {
            return owner;
        }
        return owner + "/" + name;
    }
}

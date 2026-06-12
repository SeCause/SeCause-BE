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
            AnalysisStatus analysisStatus,
            int progressPercent,
            LocalDateTime analysisRequestedAt,
            LocalDateTime completedAt
    ) {
        return new RepositorySummaryResponse(
                repositoryId,
                owner,
                name,
                owner + "/" + name,
                branch,
                fileCount,
                lineCount,
                languages,
                analysisStatus,
                progressPercent,
                analysisRequestedAt,
                completedAt
        );
    }
}

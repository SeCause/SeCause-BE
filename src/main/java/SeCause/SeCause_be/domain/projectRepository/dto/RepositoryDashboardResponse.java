package SeCause.SeCause_be.domain.projectRepository.dto;

import java.util.List;

public record RepositoryDashboardResponse(
        Long repositoryId,
        String owner,
        String name,
        String fullName,
        String description,
        String githubUrl,
        RepositoryCodeDetailsResponse codeDetails,
        RepositoryAnalysisResponse analysis,
        RepositoryDashboardSummaryResponse summary,
        List<RepositoryIssueTypeCountResponse> issuesByType,
        List<RepositorySeverityBreakdownResponse> severityBreakdown
) {
}

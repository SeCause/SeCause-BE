package SeCause.SeCause_be.domain.projectRepository.dto;

public record RepositoryDashboardSummaryResponse(
        long totalIssues,
        long criticalIssues,
        long highIssues,
        long mediumIssues,
        long lowIssues
) {
}

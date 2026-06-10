package SeCause.SeCause_be.domain.analysis.dto;

public record FastApiAnalysisRequest(
        Long analysisId,
        Long repositoryId,
        String repositoryUrl,
        String branch,
        String githubToken
) {
}

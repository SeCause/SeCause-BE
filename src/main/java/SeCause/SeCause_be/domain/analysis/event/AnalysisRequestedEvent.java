package SeCause.SeCause_be.domain.analysis.event;

public record AnalysisRequestedEvent(
        Long analysisId,
        Long repositoryId,
        String repositoryUrl,
        String branch,
        String githubToken
) {
}

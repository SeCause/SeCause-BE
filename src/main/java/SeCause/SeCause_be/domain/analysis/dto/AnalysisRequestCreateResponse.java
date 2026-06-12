package SeCause.SeCause_be.domain.analysis.dto;

import SeCause.SeCause_be.domain.analysis.entity.Analysis;
import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import SeCause.SeCause_be.domain.projectRepository.entity.ProjectRepository;

public record AnalysisRequestCreateResponse(
        Long analysisId,
        Long repositoryId,
        AnalysisStatus analysisStatus
) {

    public static AnalysisRequestCreateResponse of(Analysis analysis, ProjectRepository repository) {
        return new AnalysisRequestCreateResponse(
                analysis.getAnalysisId(),
                repository.getRepositoryId(),
                analysis.getAnalysisStatus()
        );
    }
}

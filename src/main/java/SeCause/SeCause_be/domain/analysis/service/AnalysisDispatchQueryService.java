package SeCause.SeCause_be.domain.analysis.service;

import SeCause.SeCause_be.domain.analysis.dto.FastApiAnalysisRequest;
import SeCause.SeCause_be.domain.analysis.entity.Analysis;
import SeCause.SeCause_be.domain.analysis.exception.AnalysisException;
import SeCause.SeCause_be.domain.analysis.exception.code.AnalysisErrorCode;
import SeCause.SeCause_be.domain.analysis.repository.AnalysisRepository;
import SeCause.SeCause_be.domain.projectRepository.entity.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalysisDispatchQueryService {

    private final AnalysisRepository analysisRepository;

    @Transactional(readOnly = true)
    public FastApiAnalysisRequest createFastApiRequest(Long analysisId) {
        Analysis analysis = getAnalysisWithRepositoryAndUser(analysisId);
        ProjectRepository repository = analysis.getRepository();
        return new FastApiAnalysisRequest(
                analysis.getAnalysisId(),
                repository.getRepositoryId(),
                repository.getGithubLink(),
                repository.getBranch(),
                repository.getUser().getGithubToken()
        );
    }

    private Analysis getAnalysisWithRepositoryAndUser(Long analysisId) {
        return analysisRepository.findWithRepositoryAndUserByAnalysisId(analysisId)
                .orElseThrow(() -> new AnalysisException(AnalysisErrorCode.ANALYSIS_RESULT_NOT_FOUND));
    }
}

package SeCause.SeCause_be.domain.analysis.service;

import SeCause.SeCause_be.domain.analysis.client.FastApiAnalysisClient;
import SeCause.SeCause_be.domain.analysis.exception.code.AnalysisErrorCode;
import SeCause.SeCause_be.domain.analysis.dto.FastApiAnalysisRequest;
import SeCause.SeCause_be.domain.analysis.entity.Analysis;
import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import SeCause.SeCause_be.domain.analysis.exception.AnalysisException;
import SeCause.SeCause_be.domain.analysis.repository.AnalysisRepository;
import SeCause.SeCause_be.domain.projectRepository.entity.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalysisDispatchService {

    private final AnalysisRepository analysisRepository;
    private final FastApiAnalysisClient fastApiAnalysisClient;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markInProgress(Long analysisId) {
        Analysis analysis = getAnalysis(analysisId);
        analysis.updateProgress(AnalysisStatus.IN_PROGRESS, 0);
    }

    public void dispatch(Long analysisId) {
        fastApiAnalysisClient.requestAnalysis(createFastApiRequest(analysisId));
    }

    @Transactional(readOnly = true)
    public FastApiAnalysisRequest createFastApiRequest(Long analysisId) {
        Analysis analysis = getAnalysisWithRepository(analysisId);
        ProjectRepository repository = analysis.getRepository();
        return new FastApiAnalysisRequest(
                analysis.getAnalysisId(),
                repository.getRepositoryId(),
                repository.getGithubLink(),
                repository.getBranch()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long analysisId, String failureReason) {
        Analysis analysis = getAnalysis(analysisId);
        analysis.fail(failureReason);
    }

    private Analysis getAnalysis(Long analysisId) {
        return analysisRepository.findById(analysisId)
                .orElseThrow(() -> new AnalysisException(AnalysisErrorCode.ANALYSIS_RESULT_NOT_FOUND));
    }

    private Analysis getAnalysisWithRepository(Long analysisId) {
        return analysisRepository.findWithRepositoryByAnalysisId(analysisId)
                .orElseThrow(() -> new AnalysisException(AnalysisErrorCode.ANALYSIS_RESULT_NOT_FOUND));
    }
}

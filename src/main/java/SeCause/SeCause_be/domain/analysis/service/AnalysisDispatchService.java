package SeCause.SeCause_be.domain.analysis.service;

import SeCause.SeCause_be.domain.analysis.client.FastApiAnalysisClient;
import SeCause.SeCause_be.domain.analysis.code.AnalysisErrorCode;
import SeCause.SeCause_be.domain.analysis.dto.FastApiAnalysisRequest;
import SeCause.SeCause_be.domain.analysis.entity.Analysis;
import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import SeCause.SeCause_be.domain.analysis.event.AnalysisRequestedEvent;
import SeCause.SeCause_be.domain.analysis.exception.AnalysisException;
import SeCause.SeCause_be.domain.analysis.repository.AnalysisRepository;
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

    public void dispatch(AnalysisRequestedEvent event) {
        fastApiAnalysisClient.requestAnalysis(new FastApiAnalysisRequest(
                event.analysisId(),
                event.repositoryId(),
                event.repositoryUrl(),
                event.branch(),
                event.githubToken()
        ));
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
}

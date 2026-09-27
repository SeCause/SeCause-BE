package SeCause.SeCause_be.domain.analysis.service;

import SeCause.SeCause_be.domain.analysis.dto.AnalysisCallbackFailureRequest;
import SeCause.SeCause_be.domain.analysis.dto.AnalysisCallbackSuccessRequest;
import SeCause.SeCause_be.domain.analysis.entity.Analysis;
import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import SeCause.SeCause_be.domain.analysis.exception.AnalysisException;
import SeCause.SeCause_be.domain.analysis.exception.code.AnalysisErrorCode;
import SeCause.SeCause_be.domain.analysis.repository.AnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisCallbackService {

    private static final String PARTIAL_FAILURE_PREFIX = "일부 스캐너 실패: ";
    private static final int MAX_FAILURE_REASON_LENGTH = 500;

    private final AnalysisRepository analysisRepository;
    private final AnalysisFindingPersistenceService analysisFindingPersistenceService;

    @Transactional
    public void handleSuccess(Long analysisId, AnalysisCallbackSuccessRequest request) {
        validateAnalysisId(analysisId, request.analysisId());
        if (request.status() != AnalysisStatus.COMPLETED) {
            throw new AnalysisException(AnalysisErrorCode.ANALYSIS_CALLBACK_INVALID_STATUS);
        }

        Analysis analysis = getAnalysisForUpdate(analysisId);
        validateRepositoryId(analysis, request.repositoryId());
        if (isTerminal(analysis.getAnalysisStatus())) {
            return;
        }

        analysisFindingPersistenceService.saveAll(analysis, request.findings());
        recordFailedScanners(analysisId, analysis, request.failedScanners());
        analysis.complete();
    }

    @Transactional
    public void handleFailure(Long analysisId, AnalysisCallbackFailureRequest request) {
        validateAnalysisId(analysisId, request.analysisId());
        if (request.status() != AnalysisStatus.FAILED) {
            throw new AnalysisException(AnalysisErrorCode.ANALYSIS_CALLBACK_INVALID_STATUS);
        }

        Analysis analysis = getAnalysisForUpdate(analysisId);
        validateRepositoryId(analysis, request.repositoryId());
        if (isTerminal(analysis.getAnalysisStatus())) {
            return;
        }

        analysis.fail(createFailureReason(request));
    }

    private Analysis getAnalysisForUpdate(Long analysisId) {
        return analysisRepository.findForUpdateWithRepositoryByAnalysisId(analysisId)
                .orElseThrow(() -> new AnalysisException(AnalysisErrorCode.ANALYSIS_RESULT_NOT_FOUND));
    }

    private void validateAnalysisId(Long pathAnalysisId, Long bodyAnalysisId) {
        if (!Objects.equals(pathAnalysisId, bodyAnalysisId)) {
            throw new AnalysisException(AnalysisErrorCode.ANALYSIS_CALLBACK_INVALID_PAYLOAD);
        }
    }

    private void validateRepositoryId(Analysis analysis, Long repositoryId) {
        if (!Objects.equals(analysis.getRepository().getRepositoryId(), repositoryId)) {
            throw new AnalysisException(AnalysisErrorCode.ANALYSIS_CALLBACK_INVALID_PAYLOAD);
        }
    }

    private boolean isTerminal(AnalysisStatus status) {
        return status == AnalysisStatus.COMPLETED
                || status == AnalysisStatus.FAILED
                || status == AnalysisStatus.CANCELLED;
    }

    private String createFailureReason(AnalysisCallbackFailureRequest request) {
        StringBuilder reason = new StringBuilder();

        append(reason, request.failedStage());
        append(reason, request.errorCode());
        append(reason, request.errorMessage());

        if (reason.isEmpty()) {
            return "분석 처리 중 오류가 발생했습니다.";
        }

        return reason.toString();
    }

    private void recordFailedScanners(Long analysisId, Analysis analysis, List<String> failedScanners) {
        if (failedScanners.isEmpty()) {
            return;
        }

        String failureReason = limit(
                PARTIAL_FAILURE_PREFIX + String.join(", ", failedScanners),
                MAX_FAILURE_REASON_LENGTH
        );
        analysis.updateFailureReason(failureReason);
        log.warn("Analysis {} completed with scanner failures: {}", analysisId, failureReason);
    }

    private void append(StringBuilder builder, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }

        if (!builder.isEmpty()) {
            builder.append(" - ");
        }
        builder.append(value.trim());
    }

    private String limit(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}

package SeCause.SeCause_be.domain.analysis.dto;

import SeCause.SeCause_be.domain.analysis.entity.Analysis;
import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;

public record AnalysisRequestStatusResponse(
        Long analysisId,
        String analysisStatus,
        int progressPercent,
        String failureReason
) {

    public static AnalysisRequestStatusResponse from(Analysis analysis) {
        return new AnalysisRequestStatusResponse(
                analysis.getAnalysisId(),
                analysis.getAnalysisStatus().getDescription(),
                analysis.getProgressPercent(),
                analysis.getFailureReason()
        );
    }
}

package SeCause.SeCause_be.domain.analysis.dto;

import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import jakarta.validation.constraints.NotNull;

public record AnalysisCallbackFailureRequest(
        @NotNull(message = "분석 ID는 필수입니다.")
        Long analysisId,

        @NotNull(message = "레포지토리 ID는 필수입니다.")
        Long repositoryId,

        @NotNull(message = "분석 상태는 필수입니다.")
        AnalysisStatus status,

        String errorCode,
        String errorMessage,
        String failedStage
) {
}

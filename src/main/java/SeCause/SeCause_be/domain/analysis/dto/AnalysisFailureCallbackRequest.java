package SeCause.SeCause_be.domain.analysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AnalysisFailureCallbackRequest(
        @NotNull Long analysisId,
        @NotNull Long repositoryId,
        @NotBlank @Pattern(regexp = "FAILED") String status,
        String errorCode,
        String errorMessage,
        String failedStage
) {
}

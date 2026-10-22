package SeCause.SeCause_be.domain.analysis.dto;

import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalysisCallbackSuccessRequest(
        @NotNull(message = "분석 ID는 필수입니다.")
        Long analysisId,

        @NotNull(message = "레포지토리 ID는 필수입니다.")
        Long repositoryId,

        @NotNull(message = "분석 상태는 필수입니다.")
        AnalysisStatus status,

        List<String> failedScanners,

        @Valid
        List<AnalysisCallbackFinding> findings,

        @Valid
        AnalysisCallbackSummary summary,

        @Schema(description = "선택 코드 통계. 생략/null이면 기존 통계를 유지합니다.")
        @Valid
        AnalysisCallbackCodeDetails codeDetails
) {
    public AnalysisCallbackSuccessRequest {
        failedScanners = failedScanners == null
                ? List.of()
                : failedScanners.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(scanner -> !scanner.isEmpty())
                        .toList();
    }
}

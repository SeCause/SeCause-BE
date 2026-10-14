package SeCause.SeCause_be.domain.analysis.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AnalysisProgressRequest(
        @NotNull(message = "진행률은 필수입니다.")
        @Min(value = 0, message = "진행률은 0 이상이어야 합니다.")
        @Max(value = 100, message = "진행률은 100 이하여야 합니다.")
        Integer progressPercent
) {
}

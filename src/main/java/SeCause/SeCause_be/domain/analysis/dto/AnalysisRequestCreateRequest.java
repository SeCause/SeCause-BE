package SeCause.SeCause_be.domain.analysis.dto;

import jakarta.validation.constraints.NotBlank;

public record AnalysisRequestCreateRequest(
        @NotBlank(message = "레포지토리 owner는 필수입니다.")
        String owner,

        @NotBlank(message = "레포지토리 이름은 필수입니다.")
        String repositoryName,

        @NotBlank(message = "브랜치는 필수입니다.")
        String branch
) {
}

package SeCause.SeCause_be.domain.projectRepository.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record RepositoryIssueSummaryResponse(
        @Schema(description = "분석 결과 ID", example = "1")
        Long analysisResultId,

        @Schema(description = "취약점 유형", example = "SQL_INJECTION")
        String vulnerabilityType,

        @Schema(description = "취약도 수준", example = "CRITICAL")
        String severity,

        @Schema(description = "취약점이 발견된 파일 경로", example = "src/utils/database.ts")
        String filePath,

        @Schema(description = "취약 코드 시작 라인", example = "25", nullable = true)
        Integer lineStart,

        @Schema(description = "취약 코드 종료 라인", example = "30", nullable = true)
        Integer lineEnd,

        @Schema(description = "취약점 요약", example = "사용자 입력값이 검증 없이 SQL 쿼리에 직접 사용됩니다.")
        String summary
) {

    public static RepositoryIssueSummaryResponse of(
            Long analysisResultId,
            String vulnerabilityType,
            String severity,
            String filePath,
            Integer lineStart,
            Integer lineEnd,
            String summary
    ) {
        return new RepositoryIssueSummaryResponse(
                analysisResultId,
                vulnerabilityType,
                severity,
                filePath,
                lineStart,
                lineEnd,
                summary
        );
    }
}

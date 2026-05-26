package SeCause.SeCause_be.domain.repository.dto;

import SeCause.SeCause_be.domain.repository.entity.FileType;
import io.swagger.v3.oas.annotations.media.Schema;

public record VulnerableFileSummaryResponse(
        @Schema(description = "레포지토리 파일 ID", example = "5")
        Long repositoryFileId,

        @Schema(description = "취약점이 발견된 파일 경로", example = "src/utils/database.ts")
        String filePath,

        @Schema(description = "파일 유형", example = "SOURCE")
        FileType fileType,

        @Schema(description = "파일 언어", example = "TypeScript", nullable = true)
        String language,

        @Schema(description = "파일에서 발견된 전체 이슈 수", example = "8")
        long issueCount,

        @Schema(description = "파일에서 발견된 치명도 CRITICAL 이슈 수", example = "2")
        long criticalCount
) {
}

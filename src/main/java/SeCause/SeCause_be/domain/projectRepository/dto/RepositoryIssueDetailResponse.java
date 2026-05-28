package SeCause.SeCause_be.domain.projectRepository.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record RepositoryIssueDetailResponse(
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

        @Schema(description = "취약 코드 스니펫", example = "const q = 'SELECT * FROM users WHERE id=' + userId;")
        String codeSnippet,

        @Schema(description = "취약점 설명", example = "사용자 입력값이 검증 없이 SQL 쿼리에 직접 포함됩니다.")
        String description,

        @Schema(description = "취약점 요약", example = "비매개변수화 쿼리로 인한 SQL Injection 취약점")
        String summary,

        @Schema(description = "공격 시나리오", example = "userId=1 OR 1=1 -- 입력 시 모든 사용자 데이터에 접근 가능합니다.")
        String attackScenario,

        @Schema(description = "수정 코드", example = "const q = 'SELECT * FROM users WHERE id=$1';\\ndb.query(q, [userId]);")
        String fixCode,

        @Schema(description = "수정 요약", example = "Prepared Statement로 파라미터 바인딩을 적용합니다.")
        String fixSummary,

        @Schema(description = "관련 보안 레퍼런스 목록")
        List<SecurityReferenceResponse> references
) {
}

package SeCause.SeCause_be.domain.analysis.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record AnalysisCallbackFinding(
        String tool,

        @NotBlank(message = "취약점 타입은 필수입니다.")
        String type,

        String cweId,

        @NotBlank(message = "심각도는 필수입니다.")
        String severity,

        @NotBlank(message = "파일 경로는 필수입니다.")
        String filePath,

        Integer lineStart,
        Integer lineEnd,
        String message,
        String evidence,
        String summary,
        String rootCause,
        String impact,
        String recommendation,

        @Valid
        List<AnalysisCallbackFixExample> fixExamples,

        List<String> references,

        @Valid
        List<AnalysisCallbackReferenceDocument> referenceDocuments
) {
}

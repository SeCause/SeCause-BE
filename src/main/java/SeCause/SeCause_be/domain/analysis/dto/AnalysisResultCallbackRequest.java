package SeCause.SeCause_be.domain.analysis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalysisResultCallbackRequest(
        @NotNull Long analysisId,
        @NotNull Long repositoryId,
        @NotBlank @Pattern(regexp = "COMPLETED") String status,
        List<String> failedScanners,
        @NotNull List<@Valid Finding> findings,
        @Valid Summary summary
) {
    public AnalysisResultCallbackRequest {
        failedScanners = failedScanners == null ? List.of() : List.copyOf(failedScanners);
    }

    public record Finding(
            String tool,
            @NotBlank String type,
            String cweId,
            String severity,
            @NotBlank String filePath,
            Integer lineStart,
            Integer lineEnd,
            String message,
            String evidence,
            String summary,
            String rootCause,
            String impact,
            String recommendation,
            List<@Valid FixExample> fixExamples,
            List<String> references,
            List<@Valid ReferenceDocument> referenceDocuments
    ) {
    }

    public record FixExample(
            String language,
            String vulnerableCode,
            String fixedCode,
            String explanation
    ) {
    }

    public record ReferenceDocument(
            String title,
            String url,
            String sourceType
    ) {
    }

    public record Summary(Integer totalCount) {
    }
}

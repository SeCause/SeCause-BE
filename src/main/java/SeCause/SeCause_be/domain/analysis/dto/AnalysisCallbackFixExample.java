package SeCause.SeCause_be.domain.analysis.dto;

public record AnalysisCallbackFixExample(
        String language,
        String vulnerableCode,
        String fixedCode,
        String explanation
) {
}

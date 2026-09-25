package SeCause.SeCause_be.domain.analysis.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "analysis.callback")
public record AnalysisCallbackProperties(
        String internalToken
) {
}

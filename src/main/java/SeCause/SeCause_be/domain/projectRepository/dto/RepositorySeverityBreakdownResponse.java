package SeCause.SeCause_be.domain.projectRepository.dto;

import SeCause.SeCause_be.domain.vulnerability.entity.Severity;

public record RepositorySeverityBreakdownResponse(
        Severity severity,
        long count,
        double percentage
) {
}

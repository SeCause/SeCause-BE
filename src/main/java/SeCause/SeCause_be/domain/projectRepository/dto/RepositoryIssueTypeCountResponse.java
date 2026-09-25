package SeCause.SeCause_be.domain.projectRepository.dto;

import SeCause.SeCause_be.domain.vulnerability.entity.Severity;

public record RepositoryIssueTypeCountResponse(
        String type,
        Severity severity,
        long count
) {
}

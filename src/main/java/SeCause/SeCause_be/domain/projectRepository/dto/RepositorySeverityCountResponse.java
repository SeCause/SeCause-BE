package SeCause.SeCause_be.domain.projectRepository.dto;

public record RepositorySeverityCountResponse(
        long critical,
        long high,
        long medium,
        long low
) {
}

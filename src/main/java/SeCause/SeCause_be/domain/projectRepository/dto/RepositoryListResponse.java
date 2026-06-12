package SeCause.SeCause_be.domain.projectRepository.dto;

import java.util.List;

public record RepositoryListResponse(
        List<RepositorySummaryResponse> repositories
) {

    public static RepositoryListResponse from(List<RepositorySummaryResponse> repositories) {
        return new RepositoryListResponse(repositories);
    }
}

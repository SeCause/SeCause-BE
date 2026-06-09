package SeCause.SeCause_be.domain.analysis.dto;

import java.util.List;

public record LinkableRepositoryListResponse(
        List<LinkableRepositoryResponse> repositories
) {

    public static LinkableRepositoryListResponse from(List<LinkableRepositoryResponse> repositories) {
        return new LinkableRepositoryListResponse(repositories);
    }
}

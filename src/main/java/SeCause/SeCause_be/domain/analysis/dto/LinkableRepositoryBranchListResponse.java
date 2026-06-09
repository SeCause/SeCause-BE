package SeCause.SeCause_be.domain.analysis.dto;

import java.util.List;

public record LinkableRepositoryBranchListResponse(
        List<LinkableRepositoryBranchResponse> branches
) {

    public static LinkableRepositoryBranchListResponse from(List<LinkableRepositoryBranchResponse> branches) {
        return new LinkableRepositoryBranchListResponse(branches);
    }
}

package SeCause.SeCause_be.domain.analysis.dto;

public record LinkableRepositoryBranchResponse(
        String name
) {

    public static LinkableRepositoryBranchResponse from(GithubBranchResponse githubBranch) {
        return new LinkableRepositoryBranchResponse(githubBranch.name());
    }
}

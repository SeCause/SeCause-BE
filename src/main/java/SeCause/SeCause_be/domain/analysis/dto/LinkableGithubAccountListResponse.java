package SeCause.SeCause_be.domain.analysis.dto;

import java.util.List;

public record LinkableGithubAccountListResponse(
        List<LinkableGithubAccountResponse> accounts
) {

    public static LinkableGithubAccountListResponse from(List<LinkableGithubAccountResponse> accounts) {
        return new LinkableGithubAccountListResponse(accounts);
    }
}

package SeCause.SeCause_be.domain.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubRepositoryResponse(
        String name,
        GithubAccountResponse owner,
        @JsonProperty("default_branch")
        String defaultBranch,
        @JsonProperty("private")
        boolean privateRepository
) {
}

package SeCause.SeCause_be.domain.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubRepositoryResponse(
        String name,
        GithubAccountResponse owner,
        String description,
        @JsonProperty("default_branch")
        String defaultBranch,
        @JsonProperty("clone_url")
        String cloneUrl,
        @JsonProperty("html_url")
        String htmlUrl,
        @JsonProperty("private")
        boolean privateRepository
) {
}

package SeCause.SeCause_be.domain.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LinkableRepositoryResponse(
        String name,
        String owner,
        String defaultBranch,
        String githubUrl,
        @JsonProperty("private")
        boolean privateRepository
) {

    public static LinkableRepositoryResponse from(GithubRepositoryResponse githubRepository) {
        return new LinkableRepositoryResponse(
                githubRepository.name(),
                githubRepository.owner().login(),
                githubRepository.defaultBranch(),
                githubRepository.htmlUrl(),
                githubRepository.privateRepository()
        );
    }
}

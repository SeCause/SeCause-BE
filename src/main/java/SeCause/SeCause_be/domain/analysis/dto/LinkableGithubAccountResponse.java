package SeCause.SeCause_be.domain.analysis.dto;

public record LinkableGithubAccountResponse(
        String name,
        LinkableGithubAccountType type
) {

    public static LinkableGithubAccountResponse personal(String name) {
        return new LinkableGithubAccountResponse(name, LinkableGithubAccountType.PERSONAL);
    }

    public static LinkableGithubAccountResponse organization(String name) {
        return new LinkableGithubAccountResponse(name, LinkableGithubAccountType.ORGANIZATION);
    }
}

package SeCause.SeCause_be.domain.analysis.service;

import SeCause.SeCause_be.domain.analysis.client.GithubRepositoryClient;
import SeCause.SeCause_be.domain.analysis.dto.GithubAccountResponse;
import SeCause.SeCause_be.domain.analysis.dto.GithubBranchResponse;
import SeCause.SeCause_be.domain.analysis.dto.GithubRepositoryResponse;
import SeCause.SeCause_be.domain.analysis.dto.GithubUserAccountResponse;
import SeCause.SeCause_be.domain.analysis.dto.LinkableGithubAccountListResponse;
import SeCause.SeCause_be.domain.analysis.dto.LinkableGithubAccountResponse;
import SeCause.SeCause_be.domain.analysis.dto.LinkableRepositoryBranchListResponse;
import SeCause.SeCause_be.domain.analysis.dto.LinkableRepositoryBranchResponse;
import SeCause.SeCause_be.domain.analysis.dto.LinkableRepositoryListResponse;
import SeCause.SeCause_be.domain.analysis.dto.LinkableRepositoryResponse;
import SeCause.SeCause_be.domain.analysis.validator.AnalysisRequestValidator;
import SeCause.SeCause_be.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisRequestService {

    private final GithubRepositoryClient githubRepositoryClient;
    private final AnalysisRequestValidator analysisRequestValidator;

    public LinkableGithubAccountListResponse getLinkableGithubAccounts(Long userId) {
        User user = analysisRequestValidator.validateLoginUser(userId);
        String githubToken = analysisRequestValidator.validateGithubToken(user.getGithubToken());

        GithubUserAccountResponse userAccount = githubRepositoryClient.getUserAccount(githubToken);

        List<LinkableGithubAccountResponse> organizationAccounts = githubRepositoryClient
                .getUserOrganizations(githubToken)
                .stream()
                .map(GithubAccountResponse::login)
                .filter(StringUtils::hasText)
                .sorted(Comparator.naturalOrder())
                .map(LinkableGithubAccountResponse::organization)
                .toList();

        List<LinkableGithubAccountResponse> accounts = new java.util.ArrayList<>();
        accounts.add(LinkableGithubAccountResponse.personal(userAccount.login()));
        accounts.addAll(organizationAccounts);

        return LinkableGithubAccountListResponse.from(accounts);
    }

    public LinkableRepositoryListResponse getLinkableRepositories(Long userId, String accountName, String keyword) {
        User user = analysisRequestValidator.validateLoginUser(userId);
        String githubToken = analysisRequestValidator.validateGithubToken(user.getGithubToken());
        String validatedAccountName = analysisRequestValidator.validateGithubAccountName(accountName);

        GithubUserAccountResponse userAccount = githubRepositoryClient.getUserAccount(githubToken);
        List<GithubRepositoryResponse> githubRepositories;
        if (validatedAccountName.equals(userAccount.login())) {
            githubRepositories = githubRepositoryClient.getUserOwnedRepositories(githubToken);
        } else {
            analysisRequestValidator.validateOrganizationAccount(
                    githubRepositoryClient.getUserOrganizations(githubToken),
                    validatedAccountName
            );
            githubRepositories = githubRepositoryClient.getOrganizationRepositories(
                    githubToken,
                    validatedAccountName
            );
        }

        Map<String, LinkableRepositoryResponse> repositories = new LinkedHashMap<>();
        addRepositories(repositories, githubRepositories);

        List<LinkableRepositoryResponse> responses = repositories.values().stream()
                .filter(repository -> matchesKeyword(repository, keyword))
                .sorted(Comparator.comparing(LinkableRepositoryResponse::name))
                .toList();

        return LinkableRepositoryListResponse.from(responses);
    }

    public LinkableRepositoryBranchListResponse getLinkableRepositoryBranches(
            Long userId,
            String owner,
            String repository
    ) {
        User user = analysisRequestValidator.validateLoginUser(userId);
        String githubToken = analysisRequestValidator.validateGithubToken(user.getGithubToken());

        List<LinkableRepositoryBranchResponse> branches = githubRepositoryClient
                .getRepositoryBranches(githubToken, owner, repository)
                .stream()
                .filter(this::isLinkableBranch)
                .map(LinkableRepositoryBranchResponse::from)
                .sorted(Comparator.comparing(LinkableRepositoryBranchResponse::name))
                .toList();

        return LinkableRepositoryBranchListResponse.from(branches);
    }

    private void addRepositories(
            Map<String, LinkableRepositoryResponse> repositories,
            List<GithubRepositoryResponse> githubRepositories
    ) {
        githubRepositories.stream()
                .filter(this::isLinkableRepository)
                .map(LinkableRepositoryResponse::from)
                .forEach(repository -> repositories.putIfAbsent(
                        repository.owner() + "/" + repository.name(),
                        repository
                ));
    }

    private boolean isLinkableRepository(GithubRepositoryResponse repository) {
        return StringUtils.hasText(repository.name())
                && repository.owner() != null
                && StringUtils.hasText(repository.owner().login())
                && StringUtils.hasText(repository.defaultBranch());
    }

    private boolean matchesKeyword(LinkableRepositoryResponse repository, String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return true;
        }

        String normalizedKeyword = keyword.trim().toLowerCase();
        return repository.name().toLowerCase().contains(normalizedKeyword)
                || repository.owner().toLowerCase().contains(normalizedKeyword);
    }

    private boolean isLinkableBranch(GithubBranchResponse branch) {
        return StringUtils.hasText(branch.name());
    }
}

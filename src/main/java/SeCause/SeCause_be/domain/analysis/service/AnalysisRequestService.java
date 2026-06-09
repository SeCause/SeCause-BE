package SeCause.SeCause_be.domain.analysis.service;

import SeCause.SeCause_be.domain.analysis.client.GithubRepositoryClient;
import SeCause.SeCause_be.domain.analysis.dto.GithubAccountResponse;
import SeCause.SeCause_be.domain.analysis.dto.GithubUserAccountResponse;
import SeCause.SeCause_be.domain.analysis.dto.LinkableGithubAccountListResponse;
import SeCause.SeCause_be.domain.analysis.dto.LinkableGithubAccountResponse;
import SeCause.SeCause_be.domain.analysis.validator.AnalysisRequestValidator;
import SeCause.SeCause_be.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;

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
}

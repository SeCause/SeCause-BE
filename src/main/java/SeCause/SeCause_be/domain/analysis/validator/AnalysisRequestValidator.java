package SeCause.SeCause_be.domain.analysis.validator;

import SeCause.SeCause_be.domain.analysis.client.GithubRepositoryClient;
import SeCause.SeCause_be.domain.analysis.dto.GithubUserAccountResponse;
import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import SeCause.SeCause_be.domain.analysis.exception.code.AnalysisErrorCode;
import SeCause.SeCause_be.domain.analysis.dto.GithubAccountResponse;
import SeCause.SeCause_be.domain.analysis.exception.AnalysisException;
import SeCause.SeCause_be.domain.analysis.repository.AnalysisRepository;
import SeCause.SeCause_be.domain.user.code.UserErrorCode;
import SeCause.SeCause_be.domain.user.entity.User;
import SeCause.SeCause_be.domain.user.exception.UserException;
import SeCause.SeCause_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AnalysisRequestValidator {

    private final UserRepository userRepository;
    private final AnalysisRepository analysisRepository;
    private final GithubRepositoryClient githubRepositoryClient;

    private static final List<AnalysisStatus> ACTIVE_ANALYSIS_STATUSES = List.of(
            AnalysisStatus.PENDING,
            AnalysisStatus.IN_PROGRESS
    );

    public User validateLoginUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    public String validateGithubToken(String githubToken) {
        if (!StringUtils.hasText(githubToken)) {
            throw new UserException(UserErrorCode.GITHUB_TOKEN_NOT_FOUND);
        }

        return githubToken;
    }

    public String validateGithubAccountName(String accountName) {
        if (!StringUtils.hasText(accountName)) {
            throw new AnalysisException(AnalysisErrorCode.GITHUB_ACCOUNT_NOT_FOUND);
        }

        return accountName.trim(); //혹시 모를 공백 제거
    }

    public void validateOrganizationAccount(
            List<GithubAccountResponse> organizations,
            String accountName
    ) {
        boolean exists = organizations.stream()
                .map(GithubAccountResponse::login)
                .filter(StringUtils::hasText)
                .anyMatch(login -> login.equalsIgnoreCase(accountName.trim()));


        if (!exists) {
            throw new AnalysisException(AnalysisErrorCode.GITHUB_ACCOUNT_NOT_FOUND);
        }
    }

    //접근 권한 검증
    public void validateRepositoryOwnerAccess(String githubToken, String owner) {
        GithubUserAccountResponse userAccount = githubRepositoryClient.getUserAccount(githubToken);
        if (userAccount.login().equalsIgnoreCase(owner)) {
            return;
        }

        validateOrganizationAccount(
                githubRepositoryClient.getUserOrganizations(githubToken),
                owner
        );
    }

    public void validateNoActiveAnalysis(Long userId, String githubLink, String branch) {
        boolean exists = analysisRepository.existsByRepositoryGithubLinkAndRepositoryBranchAndRepositoryUserUserIdAndRepositoryDeletedFalseAndAnalysisStatusIn(
                githubLink,
                branch,
                userId,
                ACTIVE_ANALYSIS_STATUSES
        );
        if (exists) {
            throw new AnalysisException(AnalysisErrorCode.ANALYSIS_ALREADY_IN_PROGRESS);
        }
    }
}

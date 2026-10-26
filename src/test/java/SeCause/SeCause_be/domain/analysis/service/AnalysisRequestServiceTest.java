package SeCause.SeCause_be.domain.analysis.service;

import SeCause.SeCause_be.domain.analysis.client.GithubRepositoryClient;
import SeCause.SeCause_be.domain.analysis.dto.GithubUserAccountResponse;
import SeCause.SeCause_be.domain.analysis.entity.Analysis;
import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import SeCause.SeCause_be.domain.analysis.repository.AnalysisRepository;
import SeCause.SeCause_be.domain.analysis.validator.AnalysisRequestValidator;
import SeCause.SeCause_be.domain.projectRepository.entity.ProjectRepository;
import SeCause.SeCause_be.domain.user.entity.User;
import SeCause.SeCause_be.domain.user.repository.UserRepository;
import SeCause.SeCause_be.support.TransactionTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AnalysisRequestServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final AnalysisRepository analyses = mock(AnalysisRepository.class);
    private final GithubRepositoryClient client = mock(GithubRepositoryClient.class);
    private final AnalysisRequestService service = TransactionTestSupport.proxy(new AnalysisRequestService(
            client, new AnalysisRequestValidator(users, analyses, client),
            mock(AnalysisRequestPersistenceService.class), analyses));

    @ParameterizedTest
    @ValueSource(strings = {"accounts", "repositories", "branches"})
    void githubLookupsRunOutsideTransaction(String operation) {
        when(users.findById(1L)).thenReturn(Optional.of(user()));
        when(client.getUserAccount("token")).thenAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            return new GithubUserAccountResponse("owner");
        });
        when(client.getUserOrganizations("token")).thenAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            return List.of();
        });
        when(client.getUserOwnedRepositories("token")).thenAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            return List.of();
        });
        when(client.getRepositoryBranches("token", "owner", "repo")).thenAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            return List.of();
        });

        switch (operation) {
            case "accounts" -> assertThat(service.getLinkableGithubAccounts(1L).accounts()).hasSize(1);
            case "repositories" -> assertThat(service.getLinkableRepositories(1L, "owner", null).repositories()).isEmpty();
            case "branches" -> assertThat(service.getLinkableRepositoryBranches(1L, "owner", "repo").branches()).isEmpty();
            default -> throw new AssertionError(operation);
        }
    }

    @Test
    void statusLookupKeepsReadOnlyTransaction() {
        ProjectRepository project = ProjectRepository.create(user(), "owner", "repo", null, "url", "main");
        Analysis analysis = Analysis.create(project);
        ReflectionTestUtils.setField(analysis, "analysisId", 2L);
        when(analyses.findWithRepositoryAndUserByAnalysisId(2L)).thenAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
            assertThat(TransactionSynchronizationManager.isCurrentTransactionReadOnly()).isTrue();
            return Optional.of(analysis);
        });

        var response = service.getAnalysisRequestStatus(1L, 2L);

        assertThat(response.analysisId()).isEqualTo(2L);
        assertThat(response.analysisStatus()).isEqualTo(AnalysisStatus.PENDING);
        assertThat(response.progressPercent()).isZero();
        verifyNoInteractions(client);
    }

    private User user() {
        User user = User.createGithubUser(1L, "owner", null, "Owner", "token", null);
        ReflectionTestUtils.setField(user, "userId", 1L);
        return user;
    }
}

package SeCause.SeCause_be.domain.analysis.service;

import SeCause.SeCause_be.domain.analysis.client.FastApiAnalysisClient;
import SeCause.SeCause_be.domain.analysis.dto.FastApiAnalysisRequest;
import SeCause.SeCause_be.domain.analysis.entity.Analysis;
import SeCause.SeCause_be.domain.analysis.exception.AnalysisException;
import SeCause.SeCause_be.domain.analysis.queue.AnalysisRequestQueueConsumer;
import SeCause.SeCause_be.domain.analysis.queue.AnalysisRequestQueueMessage;
import SeCause.SeCause_be.domain.analysis.repository.AnalysisRepository;
import SeCause.SeCause_be.domain.projectRepository.entity.ProjectRepository;
import SeCause.SeCause_be.domain.user.entity.User;
import SeCause.SeCause_be.support.TransactionTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnalysisDispatchServiceTest {

    private final AnalysisRepository repository = mock(AnalysisRepository.class);
    private final FastApiAnalysisClient client = mock(FastApiAnalysisClient.class);
    private final AnalysisDispatchService service = TransactionTestSupport.proxy(
            new AnalysisDispatchService(repository, client));

    @Test
    void readsRequestInTransactionAndSendsAfterTransactionEnds() {
        User user = User.createGithubUser(1L, "owner", null, "Owner", "token", null);
        ProjectRepository project = ProjectRepository.create(user, "owner", "repo", null, "url", "main");
        ReflectionTestUtils.setField(project, "repositoryId", 2L);
        Analysis analysis = Analysis.create(project);
        ReflectionTestUtils.setField(analysis, "analysisId", 3L);
        when(repository.findById(3L)).thenReturn(Optional.of(analysis));
        when(repository.findWithRepositoryAndUserByAnalysisId(3L)).thenAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
            assertThat(TransactionSynchronizationManager.isCurrentTransactionReadOnly()).isTrue();
            return Optional.of(analysis);
        });
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            assertThat((FastApiAnalysisRequest) invocation.getArgument(0))
                    .isEqualTo(new FastApiAnalysisRequest(3L, 2L, "url", "main", "token"));
            return null;
        }).when(client).requestAnalysis(any());

        new AnalysisRequestQueueConsumer(service).consume(new AnalysisRequestQueueMessage(3L));

        verify(client).requestAnalysis(any());
    }

    @Test
    void missingAnalysisDoesNotSendRequest() {
        when(repository.findWithRepositoryAndUserByAnalysisId(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createFastApiRequest(3L)).isInstanceOf(AnalysisException.class);

        verifyNoInteractions(client);
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
    }
}

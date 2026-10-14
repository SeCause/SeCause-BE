package SeCause.SeCause_be.domain.analysis.service;

import SeCause.SeCause_be.domain.analysis.dto.AnalysisProgressRequest;
import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import SeCause.SeCause_be.domain.analysis.exception.AnalysisException;
import SeCause.SeCause_be.domain.analysis.repository.AnalysisRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AnalysisProgressServiceTest {
    private final AnalysisRepository repository = mock(AnalysisRepository.class);
    private final AnalysisCallbackService service = new AnalysisCallbackService(
            repository, mock(AnalysisFindingPersistenceService.class));

    @Test
    void acceptsUpdatedAndIgnoredProgress() {
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.updateProgressIfIncreasing(1L, AnalysisStatus.IN_PROGRESS, 45)).thenReturn(1, 0);
        service.handleProgress(1L, new AnalysisProgressRequest(45));
        service.handleProgress(1L, new AnalysisProgressRequest(45));
        verify(repository, times(2)).updateProgressIfIncreasing(1L, AnalysisStatus.IN_PROGRESS, 45);
    }

    @Test
    void rejectsMissingAnalysisWithoutUpdating() {
        assertThatThrownBy(() -> service.handleProgress(1L, new AnalysisProgressRequest(45)))
                .isInstanceOf(AnalysisException.class);
        verify(repository, never()).updateProgressIfIncreasing(anyLong(), any(), anyInt());
    }

    @Test
    void rejectsInvalidProgressBeforeAccessingDatabase() {
        for (Integer progress : new Integer[]{null, -1, 101}) {
            assertThatThrownBy(() -> service.handleProgress(1L, new AnalysisProgressRequest(progress)))
                    .isInstanceOf(AnalysisException.class);
        }
        verifyNoInteractions(repository);
    }
}

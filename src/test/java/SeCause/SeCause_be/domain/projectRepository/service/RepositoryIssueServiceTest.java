package SeCause.SeCause_be.domain.projectRepository.service;

import SeCause.SeCause_be.domain.analysis.repository.AnalysisResultRepository;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryIssueListResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryIssueSeverity;
import SeCause.SeCause_be.domain.projectRepository.validator.ProjectRepositoryValidator;
import SeCause.SeCause_be.domain.vulnerability.entity.Severity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RepositoryIssueServiceTest {
    @Mock
    private AnalysisResultRepository analysisResultRepository;
    @Mock
    private ProjectRepositoryValidator projectRepositoryValidator;
    @InjectMocks
    private RepositoryIssueService service;

    @Test
    void filtersByFileAndSeverityWithOneBasedPagination() {
        var pageable = PageRequest.of(1, 5);
        var expected = RepositoryIssueListResponse.from(List.of(), 2, 5, 12);
        given(analysisResultRepository.findRepositoryIssues(7L, 1L, 33L, Severity.HIGH, pageable))
                .willReturn(expected);

        var response = service.getRepositoryIssues(7L, 1L, 33L, RepositoryIssueSeverity.HIGH, 2, 5);

        verify(projectRepositoryValidator).validateRepositoryOwner(7L, 1L);
        assertThat(response).isSameAs(expected);
        assertThat(response.totalElements()).isEqualTo(12);
        assertThat(response.totalPages()).isEqualTo(3);
    }

    @Test
    void omittedFileAndAllSeverityPreserveUnfilteredQuery() {
        var pageable = PageRequest.of(0, 20);
        var expected = RepositoryIssueListResponse.from(List.of(), 1, 20, 0);
        given(analysisResultRepository.findRepositoryIssues(7L, 1L, null, null, pageable))
                .willReturn(expected);

        var response = service.getRepositoryIssues(7L, 1L, null, RepositoryIssueSeverity.ALL, 1, 20);

        verify(projectRepositoryValidator).validateRepositoryOwner(7L, 1L);
        assertThat(response).isSameAs(expected);
        assertThat(response.totalPages()).isZero();
    }
}

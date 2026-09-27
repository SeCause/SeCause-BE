package SeCause.SeCause_be.domain.analysis.service;

import SeCause.SeCause_be.domain.analysis.dto.AnalysisCallbackFailureRequest;
import SeCause.SeCause_be.domain.analysis.dto.AnalysisCallbackSuccessRequest;
import SeCause.SeCause_be.domain.analysis.entity.Analysis;
import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import SeCause.SeCause_be.domain.analysis.repository.AnalysisRepository;
import SeCause.SeCause_be.domain.projectRepository.entity.ProjectRepository;
import SeCause.SeCause_be.domain.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisCallbackServiceTest {

    private static final String SUCCESS_JSON = """
            {
              "analysisId": 1, "repositoryId": 1, "status": "COMPLETED",
              "findings": [{
                "tool": "SEMGREP", "type": "SQL_INJECTION", "cweId": "CWE-89", "severity": "HIGH",
                "filePath": "src/db/user.py", "lineStart": 25, "lineEnd": 27,
                "message": "scanner message", "evidence": "취약 코드 조각",
                "summary": "요약", "rootCause": "원인", "impact": "영향", "recommendation": "수정 방향",
                "fixExamples": [{"language":"python","vulnerableCode":"...","fixedCode":"...","explanation":"..."}],
                "references": ["https://example.com/reference"],
                "referenceDocuments": [{"title":"문서 제목","url":"https://example.com/document","sourceType":"CWE"}]
              }],
              "summary": {"totalCount": 12}
            }
            """;

    private static final String FAILURE_JSON = """
            {
              "analysisId":1, "repositoryId":1, "status":"FAILED",
              "errorCode":"CLONE_FAILED", "errorMessage":"clone failed", "failedStage":"collect"
            }
            """;

    @Mock
    private AnalysisRepository analysisRepository;
    @Mock
    private AnalysisFindingPersistenceService analysisFindingPersistenceService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private AnalysisCallbackService service;

    @BeforeEach
    void setUp() {
        service = new AnalysisCallbackService(analysisRepository, analysisFindingPersistenceService);
    }

    @Test
    void handlesPayloadWithoutFailedScannersAndIgnoresDuplicate() throws Exception {
        Analysis analysis = createAnalysis();
        analysis.getRepository().updateAnalysisMetrics(42, 1_000L);
        AnalysisCallbackSuccessRequest request = objectMapper.readValue(SUCCESS_JSON, AnalysisCallbackSuccessRequest.class);
        assertThat(request.failedScanners()).isEmpty();
        when(analysisRepository.findForUpdateWithRepositoryByAnalysisId(1L)).thenReturn(Optional.of(analysis));
        when(analysisFindingPersistenceService.saveAll(analysis, request.findings())).thenReturn(1);

        service.handleSuccess(1L, request);
        service.handleSuccess(1L, request);

        verify(analysisFindingPersistenceService, times(1)).saveAll(analysis, request.findings());
        assertThat(analysis.getAnalysisStatus()).isEqualTo(AnalysisStatus.COMPLETED);
        assertThat(analysis.getProgressPercent()).isEqualTo(100);
        assertThat(analysis.getCompletedAt()).isNotNull();
        assertThat(analysis.getRepository().getTotalFiles()).isEqualTo(42);
        assertThat(analysis.getRepository().getLineCount()).isEqualTo(1_000L);
    }

    @Test
    void completesAndPersistsFindingsWhenSomeScannersFailed() throws Exception {
        Analysis analysis = createAnalysis();
        String callbackJson = SUCCESS_JSON.replace(
                "\"status\": \"COMPLETED\",",
                "\"status\": \"COMPLETED\", \"failedScanners\": [\" TRIVY \", null, \"\", \"  \", \"CHECKOV\"],"
        );
        AnalysisCallbackSuccessRequest request = objectMapper.readValue(
                callbackJson,
                AnalysisCallbackSuccessRequest.class
        );
        when(analysisRepository.findForUpdateWithRepositoryByAnalysisId(1L)).thenReturn(Optional.of(analysis));
        when(analysisFindingPersistenceService.saveAll(analysis, request.findings())).thenReturn(1);

        service.handleSuccess(1L, request);

        verify(analysisFindingPersistenceService).saveAll(analysis, request.findings());
        assertThat(request.failedScanners()).containsExactly("TRIVY", "CHECKOV");
        assertThat(analysis.getAnalysisStatus()).isEqualTo(AnalysisStatus.COMPLETED);
        assertThat(analysis.getFailureReason()).isEqualTo("일부 스캐너 실패: TRIVY, CHECKOV");
    }

    @Test
    void handlesFailureAndIgnoresDuplicate() throws Exception {
        Analysis analysis = createAnalysis();
        AnalysisCallbackFailureRequest request = objectMapper.readValue(FAILURE_JSON, AnalysisCallbackFailureRequest.class);
        when(analysisRepository.findForUpdateWithRepositoryByAnalysisId(1L)).thenReturn(Optional.of(analysis));

        service.handleFailure(1L, request);
        service.handleFailure(1L, request);

        assertThat(analysis.getAnalysisStatus()).isEqualTo(AnalysisStatus.FAILED);
        assertThat(analysis.getFailureReason()).isEqualTo("collect - CLONE_FAILED - clone failed");
        assertThat(analysis.getCompletedAt()).isNotNull();
        verifyNoInteractions(analysisFindingPersistenceService);
    }

    private Analysis createAnalysis() {
        User user = User.createGithubUser(1L, "octocat", "octocat@example.com", "Octocat", "token", null);
        ProjectRepository repository = ProjectRepository.create(
                user,
                "octocat",
                "repo",
                "description",
                "https://github.com/octocat/repo",
                "main"
        );
        ReflectionTestUtils.setField(repository, "repositoryId", 1L);
        return Analysis.create(repository);
    }
}

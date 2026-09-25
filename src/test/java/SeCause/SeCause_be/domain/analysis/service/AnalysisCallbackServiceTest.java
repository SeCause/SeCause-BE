package SeCause.SeCause_be.domain.analysis.service;

import SeCause.SeCause_be.domain.analysis.dto.AnalysisFailureCallbackRequest;
import SeCause.SeCause_be.domain.analysis.dto.AnalysisResultCallbackRequest;
import SeCause.SeCause_be.domain.analysis.entity.Analysis;
import SeCause.SeCause_be.domain.analysis.entity.AnalysisResult;
import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import SeCause.SeCause_be.domain.analysis.repository.AnalysisRepository;
import SeCause.SeCause_be.domain.analysis.repository.AnalysisResultRepository;
import SeCause.SeCause_be.domain.projectRepository.entity.ProjectRepository;
import SeCause.SeCause_be.domain.projectRepository.repository.RepositoryFileRepository;
import SeCause.SeCause_be.domain.security.repository.SecurityReferenceRepository;
import SeCause.SeCause_be.domain.user.entity.User;
import SeCause.SeCause_be.domain.vulnerability.entity.Severity;
import SeCause.SeCause_be.domain.vulnerability.entity.Vulnerability;
import SeCause.SeCause_be.domain.vulnerability.repository.VulnerabilityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
    private RepositoryFileRepository repositoryFileRepository;
    @Mock
    private VulnerabilityRepository vulnerabilityRepository;
    @Mock
    private AnalysisResultRepository analysisResultRepository;
    @Mock
    private SecurityReferenceRepository securityReferenceRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private AnalysisCallbackService service;

    @BeforeEach
    void setUp() {
        service = new AnalysisCallbackService(
                analysisRepository,
                repositoryFileRepository,
                vulnerabilityRepository,
                analysisResultRepository,
                securityReferenceRepository
        );
    }

    @Test
    void savesSuccessfulCallbackAndIgnoresDuplicate() throws Exception {
        Analysis analysis = createAnalysis();
        AnalysisResultCallbackRequest request = objectMapper.readValue(
                SUCCESS_JSON,
                AnalysisResultCallbackRequest.class
        );
        assertThat(request.failedScanners()).isEmpty();
        when(analysisRepository.findForUpdateWithRepositoryByAnalysisId(1L)).thenReturn(Optional.of(analysis));
        when(repositoryFileRepository.findAllByRepositoryRepositoryIdAndFilePathIn(any(), any())).thenReturn(List.of());
        when(repositoryFileRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(vulnerabilityRepository.save(any(Vulnerability.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(securityReferenceRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        service.saveResult(1L, request);
        service.saveResult(1L, request);

        ArgumentCaptor<Vulnerability> vulnerabilityCaptor = ArgumentCaptor.forClass(Vulnerability.class);
        verify(vulnerabilityRepository, times(1)).save(vulnerabilityCaptor.capture());
        verify(analysisResultRepository, times(1)).save(any(AnalysisResult.class));
        verify(securityReferenceRepository, times(1)).saveAll(anyList());
        assertThat(vulnerabilityCaptor.getValue().getCweId()).isEqualTo("CWE-89");
        assertThat(vulnerabilityCaptor.getValue().getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(analysis.getAnalysisStatus()).isEqualTo(AnalysisStatus.COMPLETED);
        assertThat(analysis.getProgressPercent()).isEqualTo(100);
        assertThat(analysis.getCompletedAt()).isNotNull();
        assertThat(analysis.getRepository().getTotalFiles()).isEqualTo(1);
    }

    @Test
    void completesAndStoresVulnerabilitiesWhenSomeScannersFailed() throws Exception {
        Analysis analysis = createAnalysis();
        String callbackJson = SUCCESS_JSON.replace(
                "\"status\": \"COMPLETED\",",
                "\"status\": \"COMPLETED\", \"failedScanners\": [\"TRIVY\", \"CHECKOV\"],"
        );
        AnalysisResultCallbackRequest request = objectMapper.readValue(
                callbackJson,
                AnalysisResultCallbackRequest.class
        );
        when(analysisRepository.findForUpdateWithRepositoryByAnalysisId(1L)).thenReturn(Optional.of(analysis));
        when(repositoryFileRepository.findAllByRepositoryRepositoryIdAndFilePathIn(any(), any())).thenReturn(List.of());
        when(repositoryFileRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(vulnerabilityRepository.save(any(Vulnerability.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(securityReferenceRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        service.saveResult(1L, request);

        assertThat(analysis.getAnalysisStatus()).isEqualTo(AnalysisStatus.COMPLETED);
        assertThat(analysis.getFailureReason()).isEqualTo("일부 스캐너 실패: TRIVY, CHECKOV");
        verify(vulnerabilityRepository).save(any(Vulnerability.class));
        verify(analysisResultRepository).save(any(AnalysisResult.class));
    }

    @Test
    void savesFailureCallbackAndIgnoresDuplicate() throws Exception {
        Analysis analysis = createAnalysis();
        AnalysisFailureCallbackRequest request = objectMapper.readValue(
                FAILURE_JSON,
                AnalysisFailureCallbackRequest.class
        );
        when(analysisRepository.findForUpdateWithRepositoryByAnalysisId(1L)).thenReturn(Optional.of(analysis));

        service.saveFailure(1L, request);
        service.saveFailure(1L, request);

        assertThat(analysis.getAnalysisStatus()).isEqualTo(AnalysisStatus.FAILED);
        assertThat(analysis.getFailureReason())
                .isEqualTo("failedStage=collect, errorCode=CLONE_FAILED, errorMessage=clone failed");
        assertThat(analysis.getCompletedAt()).isNotNull();
        verify(vulnerabilityRepository, never()).save(any());
    }

    @Test
    void mapsInfoSeverityToLow() throws Exception {
        Analysis analysis = createAnalysis();
        AnalysisResultCallbackRequest request = objectMapper.readValue(
                SUCCESS_JSON.replace("\"HIGH\"", "\"INFO\""),
                AnalysisResultCallbackRequest.class
        );
        when(analysisRepository.findForUpdateWithRepositoryByAnalysisId(1L)).thenReturn(Optional.of(analysis));
        when(repositoryFileRepository.findAllByRepositoryRepositoryIdAndFilePathIn(any(), any())).thenReturn(List.of());
        when(repositoryFileRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(vulnerabilityRepository.save(any(Vulnerability.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(securityReferenceRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        service.saveResult(1L, request);

        ArgumentCaptor<Vulnerability> captor = ArgumentCaptor.forClass(Vulnerability.class);
        verify(vulnerabilityRepository).save(captor.capture());
        assertThat(captor.getValue().getSeverity()).isEqualTo(Severity.LOW);
    }

    private Analysis createAnalysis() {
        User user = User.createGithubUser(1L, "octocat", "octocat@example.com", "Octocat", "token", null);
        ProjectRepository repository = ProjectRepository.create(
                user,
                "repo",
                "description",
                "https://github.com/octocat/repo",
                "main"
        );
        return Analysis.create(repository);
    }
}

package SeCause.SeCause_be.domain.analysis.service;

import SeCause.SeCause_be.domain.analysis.dto.AnalysisCallbackFinding;
import SeCause.SeCause_be.domain.analysis.dto.AnalysisCallbackFixExample;
import SeCause.SeCause_be.domain.analysis.dto.AnalysisCallbackReferenceDocument;
import SeCause.SeCause_be.domain.analysis.entity.Analysis;
import SeCause.SeCause_be.domain.analysis.entity.AnalysisResult;
import SeCause.SeCause_be.domain.analysis.repository.AnalysisResultRepository;
import SeCause.SeCause_be.domain.projectRepository.entity.FileType;
import SeCause.SeCause_be.domain.projectRepository.entity.ProjectRepository;
import SeCause.SeCause_be.domain.projectRepository.entity.RepositoryFile;
import SeCause.SeCause_be.domain.projectRepository.repository.RepositoryFileRepository;
import SeCause.SeCause_be.domain.security.entity.ReferenceType;
import SeCause.SeCause_be.domain.security.entity.SecurityReference;
import SeCause.SeCause_be.domain.security.repository.SecurityReferenceRepository;
import SeCause.SeCause_be.domain.user.entity.User;
import SeCause.SeCause_be.domain.vulnerability.entity.CodeVulnerability;
import SeCause.SeCause_be.domain.vulnerability.entity.InfraVulnerability;
import SeCause.SeCause_be.domain.vulnerability.entity.Severity;
import SeCause.SeCause_be.domain.vulnerability.entity.Vulnerability;
import SeCause.SeCause_be.domain.vulnerability.repository.CodeVulnerabilityRepository;
import SeCause.SeCause_be.domain.vulnerability.repository.InfraVulnerabilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisFindingPersistenceServiceTest {

    @Mock
    private RepositoryFileRepository repositoryFileRepository;
    @Mock
    private CodeVulnerabilityRepository codeVulnerabilityRepository;
    @Mock
    private InfraVulnerabilityRepository infraVulnerabilityRepository;
    @Mock
    private AnalysisResultRepository analysisResultRepository;
    @Mock
    private SecurityReferenceRepository securityReferenceRepository;

    private AnalysisFindingPersistenceService service;

    @BeforeEach
    void setUp() {
        service = new AnalysisFindingPersistenceService(
                repositoryFileRepository,
                codeVulnerabilityRepository,
                infraVulnerabilityRepository,
                analysisResultRepository,
                securityReferenceRepository
        );
        when(repositoryFileRepository.findAllByRepositoryRepositoryIdAndFilePathIn(any(), any())).thenReturn(List.of());
        when(repositoryFileRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(codeVulnerabilityRepository.save(any(CodeVulnerability.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void savesCweAnalysisResultAndReference(boolean infra) {
        Analysis analysis = createAnalysis();
        AnalysisCallbackFinding finding = finding("HIGH", "src/db/user.py", infra);
        if (infra) {
            when(infraVulnerabilityRepository.save(any(InfraVulnerability.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
        }

        int totalFiles = service.saveAll(analysis, List.of(finding));

        Vulnerability vulnerability;
        if (infra) {
            ArgumentCaptor<InfraVulnerability> captor = ArgumentCaptor.forClass(InfraVulnerability.class);
            verify(infraVulnerabilityRepository).save(captor.capture());
            vulnerability = captor.getValue();
        } else {
            ArgumentCaptor<CodeVulnerability> captor = ArgumentCaptor.forClass(CodeVulnerability.class);
            verify(codeVulnerabilityRepository).save(captor.capture());
            vulnerability = captor.getValue();
            assertThat(captor.getValue().getLineStart()).isEqualTo(25);
            assertThat(captor.getValue().getLineEnd()).isEqualTo(27);
        }
        assertThat(vulnerability.getCweId()).isEqualTo("CWE-89");
        assertThat(vulnerability.getSeverity()).isEqualTo(Severity.HIGH);

        ArgumentCaptor<AnalysisResult> resultCaptor = ArgumentCaptor.forClass(AnalysisResult.class);
        verify(analysisResultRepository).save(resultCaptor.capture());
        AnalysisResult result = resultCaptor.getValue();
        assertThat(result.getVulnerability()).isSameAs(vulnerability);
        assertThat(result.getDescription()).isEqualTo("원인");
        assertThat(result.getSummary()).isEqualTo("요약");
        assertThat(result.getAttackScenario()).isEqualTo("영향");
        assertThat(result.getFixCode()).isEqualTo("fixed");
        assertThat(result.getFixSummary()).isEqualTo("수정 방향");

        ArgumentCaptor<SecurityReference> referenceCaptor = ArgumentCaptor.forClass(SecurityReference.class);
        verify(securityReferenceRepository).save(referenceCaptor.capture());
        assertThat(referenceCaptor.getValue().getReferenceType()).isEqualTo(ReferenceType.CWE);
        assertThat(referenceCaptor.getValue().getVulnerability()).isSameAs(vulnerability);
        assertThat(referenceCaptor.getValue().getTitle()).isEqualTo("문서 제목");
        assertThat(referenceCaptor.getValue().getReferenceUrl()).isEqualTo("https://example.com/document");
        assertThat(totalFiles).isEqualTo(1);
    }

    @Test
    void mapsInfoAndUnknownSeverityToLow() {
        Analysis analysis = createAnalysis();

        service.saveAll(analysis, List.of(
                finding("INFO", "src/info.py"),
                finding("UNSUPPORTED", "src/unknown.py")
        ));

        ArgumentCaptor<CodeVulnerability> captor = ArgumentCaptor.forClass(CodeVulnerability.class);
        verify(codeVulnerabilityRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(CodeVulnerability::getSeverity)
                .containsExactly(Severity.LOW, Severity.LOW);
    }

    @Test
    void createsInfraFileWithLanguageMetadata() {
        Analysis analysis = createAnalysis();
        AnalysisCallbackFinding finding = new AnalysisCallbackFinding(
                "INFRA",
                "MISCONFIGURATION",
                null,
                "MEDIUM",
                "infra/main.tf",
                null,
                null,
                "scanner message",
                "취약 설정",
                "요약",
                "원인",
                "영향",
                "수정 방향",
                List.of(new AnalysisCallbackFixExample("terraform", "...", "fixed", "설명")),
                List.of(),
                List.of()
        );

        service.saveAll(analysis, List.of(finding));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<RepositoryFile>> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(repositoryFileRepository).saveAll(captor.capture());
        RepositoryFile savedFile = StreamSupport.stream(captor.getValue().spliterator(), false)
                .findFirst()
                .orElseThrow();
        assertThat(savedFile.getFileType()).isEqualTo(FileType.INFRA);
        assertThat(savedFile.getLanguage()).isEqualTo("terraform");
        verify(infraVulnerabilityRepository).save(any(InfraVulnerability.class));
    }

    private AnalysisCallbackFinding finding(String severity, String filePath) {
        return finding(severity, filePath, false);
    }

    private AnalysisCallbackFinding finding(String severity, String filePath, boolean infra) {
        return new AnalysisCallbackFinding(
                "SEMGREP",
                "SQL_INJECTION",
                "CWE-89",
                severity,
                filePath,
                infra ? null : 25,
                infra ? null : 27,
                "scanner message",
                "취약 코드 조각",
                "요약",
                "원인",
                "영향",
                "수정 방향",
                List.of(new AnalysisCallbackFixExample("python", "...", "fixed", "설명")),
                List.of("https://example.com/reference"),
                List.of(new AnalysisCallbackReferenceDocument("문서 제목", "https://example.com/document", "CWE"))
        );
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

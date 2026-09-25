package SeCause.SeCause_be.domain.analysis.service;

import SeCause.SeCause_be.domain.analysis.dto.AnalysisFailureCallbackRequest;
import SeCause.SeCause_be.domain.analysis.dto.AnalysisResultCallbackRequest;
import SeCause.SeCause_be.domain.analysis.entity.Analysis;
import SeCause.SeCause_be.domain.analysis.entity.AnalysisResult;
import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import SeCause.SeCause_be.domain.analysis.exception.AnalysisException;
import SeCause.SeCause_be.domain.analysis.exception.code.AnalysisErrorCode;
import SeCause.SeCause_be.domain.analysis.repository.AnalysisRepository;
import SeCause.SeCause_be.domain.analysis.repository.AnalysisResultRepository;
import SeCause.SeCause_be.domain.projectRepository.entity.FileType;
import SeCause.SeCause_be.domain.projectRepository.entity.ProjectRepository;
import SeCause.SeCause_be.domain.projectRepository.entity.RepositoryFile;
import SeCause.SeCause_be.domain.projectRepository.repository.RepositoryFileRepository;
import SeCause.SeCause_be.domain.security.entity.ReferenceType;
import SeCause.SeCause_be.domain.security.entity.SecurityReference;
import SeCause.SeCause_be.domain.security.repository.SecurityReferenceRepository;
import SeCause.SeCause_be.domain.vulnerability.entity.CodeVulnerability;
import SeCause.SeCause_be.domain.vulnerability.entity.InfraVulnerability;
import SeCause.SeCause_be.domain.vulnerability.entity.Severity;
import SeCause.SeCause_be.domain.vulnerability.entity.Vulnerability;
import SeCause.SeCause_be.domain.vulnerability.repository.VulnerabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisCallbackService {

    private static final String PARTIAL_FAILURE_PREFIX = "일부 스캐너 실패: ";
    private static final int MAX_FAILURE_REASON_LENGTH = 500;

    private final AnalysisRepository analysisRepository;
    private final RepositoryFileRepository repositoryFileRepository;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final SecurityReferenceRepository securityReferenceRepository;

    @Transactional
    public void saveResult(Long analysisId, AnalysisResultCallbackRequest request) {
        Analysis analysis = getAnalysisForUpdate(analysisId);
        if (isTerminal(analysis)) {
            return;
        }

        ProjectRepository repository = analysis.getRepository();
        Map<String, RepositoryFile> filesByPath = upsertRepositoryFiles(repository, request.findings());

        for (AnalysisResultCallbackRequest.Finding finding : request.findings()) {
            RepositoryFile repositoryFile = filesByPath.get(finding.filePath());
            Vulnerability vulnerability = saveVulnerability(analysis, repositoryFile, finding);
            saveAnalysisResult(vulnerability, finding);
            saveSecurityReferences(vulnerability, finding.referenceDocuments());
        }

        recordFailedScanners(analysisId, analysis, request.failedScanners());
        repository.updateTotalFiles(filesByPath.size());
        analysis.complete();
    }

    @Transactional
    public void saveFailure(Long analysisId, AnalysisFailureCallbackRequest request) {
        Analysis analysis = getAnalysisForUpdate(analysisId);
        if (isTerminal(analysis)) {
            return;
        }

        analysis.fail(buildFailureReason(request));
    }

    private Analysis getAnalysisForUpdate(Long analysisId) {
        return analysisRepository.findForUpdateWithRepositoryByAnalysisId(analysisId)
                .orElseThrow(() -> new AnalysisException(AnalysisErrorCode.ANALYSIS_RESULT_NOT_FOUND));
    }

    private boolean isTerminal(Analysis analysis) {
        return analysis.getAnalysisStatus() == AnalysisStatus.COMPLETED
                || analysis.getAnalysisStatus() == AnalysisStatus.FAILED;
    }

    private Map<String, RepositoryFile> upsertRepositoryFiles(
            ProjectRepository repository,
            List<AnalysisResultCallbackRequest.Finding> findings
    ) {
        LinkedHashSet<String> uniquePaths = findings.stream()
                .map(AnalysisResultCallbackRequest.Finding::filePath)
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);

        Map<String, RepositoryFile> filesByPath = new LinkedHashMap<>();
        if (uniquePaths.isEmpty()) {
            return filesByPath;
        }

        repositoryFileRepository.findAllByRepositoryRepositoryIdAndFilePathIn(
                        repository.getRepositoryId(),
                        uniquePaths
                )
                .forEach(file -> filesByPath.putIfAbsent(file.getFilePath(), file));

        List<RepositoryFile> newFiles = uniquePaths.stream()
                .filter(path -> !filesByPath.containsKey(path))
                .map(path -> RepositoryFile.create(repository, path, FileType.SOURCE, null, 0L))
                .toList();

        repositoryFileRepository.saveAll(newFiles)
                .forEach(file -> filesByPath.put(file.getFilePath(), file));
        return filesByPath;
    }

    private Vulnerability saveVulnerability(
            Analysis analysis,
            RepositoryFile repositoryFile,
            AnalysisResultCallbackRequest.Finding finding
    ) {
        Severity severity = mapSeverity(finding.severity());
        Vulnerability vulnerability;

        if (finding.lineStart() != null || finding.lineEnd() != null) {
            vulnerability = CodeVulnerability.create(
                    analysis,
                    repositoryFile,
                    finding.type(),
                    finding.cweId(),
                    severity,
                    finding.lineStart(),
                    finding.lineEnd(),
                    finding.evidence()
            );
        } else {
            vulnerability = InfraVulnerability.create(
                    analysis,
                    repositoryFile,
                    finding.type(),
                    finding.cweId(),
                    severity,
                    finding.evidence()
            );
        }

        return vulnerabilityRepository.save(vulnerability);
    }

    private void saveAnalysisResult(
            Vulnerability vulnerability,
            AnalysisResultCallbackRequest.Finding finding
    ) {
        String fixCode = firstFixedCode(finding.fixExamples());
        AnalysisResult result;

        if (vulnerability instanceof CodeVulnerability codeVulnerability) {
            result = AnalysisResult.createForCodeVulnerability(
                    codeVulnerability,
                    finding.rootCause(),
                    finding.summary(),
                    finding.impact(),
                    fixCode,
                    finding.recommendation()
            );
        } else {
            result = AnalysisResult.createForInfraVulnerability(
                    (InfraVulnerability) vulnerability,
                    finding.rootCause(),
                    finding.summary(),
                    finding.impact(),
                    fixCode,
                    finding.recommendation()
            );
        }

        analysisResultRepository.save(result);
    }

    private void saveSecurityReferences(
            Vulnerability vulnerability,
            List<AnalysisResultCallbackRequest.ReferenceDocument> documents
    ) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        List<SecurityReference> references = documents.stream()
                .map(document -> createSecurityReference(vulnerability, document))
                .toList();
        securityReferenceRepository.saveAll(references);
    }

    private SecurityReference createSecurityReference(
            Vulnerability vulnerability,
            AnalysisResultCallbackRequest.ReferenceDocument document
    ) {
        ReferenceType referenceType = mapReferenceType(document.sourceType());
        if (vulnerability instanceof CodeVulnerability codeVulnerability) {
            return SecurityReference.createForCodeVulnerability(
                    codeVulnerability,
                    referenceType,
                    document.title(),
                    document.url()
            );
        }

        return SecurityReference.createForInfraVulnerability(
                (InfraVulnerability) vulnerability,
                referenceType,
                document.title(),
                document.url()
        );
    }

    private Severity mapSeverity(String value) {
        if (value == null) {
            log.warn("Unknown severity received from analysis server: null. Mapping to LOW.");
            return Severity.LOW;
        }

        try {
            if ("INFO".equalsIgnoreCase(value)) {
                return Severity.LOW;
            }
            return Severity.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            log.warn("Unknown severity received from analysis server: {}. Mapping to LOW.", value);
            return Severity.LOW;
        }
    }

    private ReferenceType mapReferenceType(String value) {
        if (value == null) {
            return ReferenceType.OTHER;
        }

        try {
            return ReferenceType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return ReferenceType.OTHER;
        }
    }

    private String firstFixedCode(List<AnalysisResultCallbackRequest.FixExample> fixExamples) {
        if (fixExamples == null || fixExamples.isEmpty()) {
            return null;
        }
        return fixExamples.getFirst().fixedCode();
    }

    private String buildFailureReason(AnalysisFailureCallbackRequest request) {
        StringJoiner reason = new StringJoiner(", ");
        addFailurePart(reason, "failedStage", request.failedStage());
        addFailurePart(reason, "errorCode", request.errorCode());
        addFailurePart(reason, "errorMessage", request.errorMessage());
        return reason.toString();
    }

    private void recordFailedScanners(Long analysisId, Analysis analysis, List<String> failedScanners) {
        if (failedScanners.isEmpty()) {
            return;
        }

        String failureReason = PARTIAL_FAILURE_PREFIX + String.join(", ", failedScanners);
        if (failureReason.length() > MAX_FAILURE_REASON_LENGTH) {
            failureReason = failureReason.substring(0, MAX_FAILURE_REASON_LENGTH);
        }

        analysis.updateFailureReason(failureReason);
        log.warn("Analysis {} completed with scanner failures: {}", analysisId, failureReason);
    }

    private void addFailurePart(StringJoiner reason, String name, String value) {
        if (Objects.nonNull(value) && !value.isBlank()) {
            reason.add(name + "=" + value);
        }
    }
}

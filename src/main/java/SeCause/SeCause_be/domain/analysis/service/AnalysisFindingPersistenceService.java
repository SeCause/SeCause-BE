package SeCause.SeCause_be.domain.analysis.service;

import SeCause.SeCause_be.domain.analysis.dto.AnalysisCallbackFinding;
import SeCause.SeCause_be.domain.analysis.dto.AnalysisCallbackFixExample;
import SeCause.SeCause_be.domain.analysis.dto.AnalysisCallbackReferenceDocument;
import SeCause.SeCause_be.domain.analysis.entity.Analysis;
import SeCause.SeCause_be.domain.analysis.entity.AnalysisResult;
import SeCause.SeCause_be.domain.analysis.repository.AnalysisResultRepository;
import SeCause.SeCause_be.domain.projectRepository.entity.FileType;
import SeCause.SeCause_be.domain.projectRepository.entity.RepositoryFile;
import SeCause.SeCause_be.domain.projectRepository.repository.RepositoryFileRepository;
import SeCause.SeCause_be.domain.security.entity.ReferenceType;
import SeCause.SeCause_be.domain.security.entity.SecurityReference;
import SeCause.SeCause_be.domain.security.repository.SecurityReferenceRepository;
import SeCause.SeCause_be.domain.vulnerability.entity.CodeVulnerability;
import SeCause.SeCause_be.domain.vulnerability.entity.InfraVulnerability;
import SeCause.SeCause_be.domain.vulnerability.entity.Severity;
import SeCause.SeCause_be.domain.vulnerability.repository.CodeVulnerabilityRepository;
import SeCause.SeCause_be.domain.vulnerability.repository.InfraVulnerabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisFindingPersistenceService {

    private final RepositoryFileRepository repositoryFileRepository;
    private final CodeVulnerabilityRepository codeVulnerabilityRepository;
    private final InfraVulnerabilityRepository infraVulnerabilityRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final SecurityReferenceRepository securityReferenceRepository;

    public int saveAll(Analysis analysis, List<AnalysisCallbackFinding> findings) {
        List<AnalysisCallbackFinding> safeFindings = safeList(findings).stream()
                .filter(Objects::nonNull)
                .toList();
        Map<String, RepositoryFile> filesByPath = upsertRepositoryFiles(analysis, safeFindings);

        safeFindings.forEach(finding -> saveFinding(
                analysis,
                finding,
                filesByPath.get(limit(finding.filePath(), 1000))
        ));
        return filesByPath.size();
    }

    // FastAPI finding을 파일, 취약점, 상세 결과, 참고 문서로 저장
    private void saveFinding(
            Analysis analysis,
            AnalysisCallbackFinding finding,
            RepositoryFile repositoryFile
    ) {
        if (finding.lineStart() == null && finding.lineEnd() == null) {
            InfraVulnerability vulnerability = infraVulnerabilityRepository.save(InfraVulnerability.create(
                    analysis,
                    repositoryFile,
                    limit(finding.type(), 100),
                    limit(finding.cweId(), 30),
                    resolveSeverity(finding.severity()),
                    finding.evidence()
            ));
            saveAnalysisResult(vulnerability, finding);
            saveSecurityReferences(vulnerability, finding.referenceDocuments());
            return;
        }

        CodeVulnerability vulnerability = codeVulnerabilityRepository.save(CodeVulnerability.create(
                analysis,
                repositoryFile,
                limit(finding.type(), 100),
                limit(finding.cweId(), 30),
                resolveSeverity(finding.severity()),
                finding.lineStart(),
                finding.lineEnd(),
                finding.evidence()
        ));
        saveAnalysisResult(vulnerability, finding);
        saveSecurityReferences(vulnerability, finding.referenceDocuments());
    }

    private Map<String, RepositoryFile> upsertRepositoryFiles(
            Analysis analysis,
            List<AnalysisCallbackFinding> findings
    ) {
        Map<String, AnalysisCallbackFinding> findingsByPath = new LinkedHashMap<>();
        findings.forEach(finding -> findingsByPath.putIfAbsent(
                limit(finding.filePath(), 1000),
                finding
        ));
        LinkedHashSet<String> uniquePaths = new LinkedHashSet<>(findingsByPath.keySet());
        Map<String, RepositoryFile> filesByPath = new LinkedHashMap<>();
        if (uniquePaths.isEmpty()) {
            return filesByPath;
        }

        repositoryFileRepository.findAllByRepositoryRepositoryIdAndFilePathIn(
                        analysis.getRepository().getRepositoryId(),
                        uniquePaths
                )
                .forEach(file -> filesByPath.putIfAbsent(file.getFilePath(), file));

        List<RepositoryFile> newFiles = uniquePaths.stream()
                .filter(path -> !filesByPath.containsKey(path))
                .map(path -> RepositoryFile.create(
                        analysis.getRepository(),
                        path,
                        resolveFileType(findingsByPath.get(path).tool()),
                        resolveLanguage(findingsByPath.get(path).fixExamples()),
                        0L
                ))
                .toList();
        repositoryFileRepository.saveAll(newFiles)
                .forEach(file -> filesByPath.put(file.getFilePath(), file));
        return filesByPath;
    }

    private FileType resolveFileType(String tool) {
        return "INFRA".equalsIgnoreCase(tool) ? FileType.INFRA : FileType.SOURCE;
    }

    // 코드 취약점 상세 결과 저장
    private void saveAnalysisResult(
            CodeVulnerability vulnerability,
            AnalysisCallbackFinding finding
    ) {
        analysisResultRepository.save(AnalysisResult.createForCodeVulnerability(
                vulnerability,
                defaultText(finding.rootCause(), finding.message(), finding.type()),
                defaultText(finding.summary(), finding.message(), finding.type()),
                finding.impact(),
                resolveFixCode(finding.fixExamples()),
                finding.recommendation()
        ));
    }

    // 인프라 취약점 상세 결과 저장
    private void saveAnalysisResult(
            InfraVulnerability vulnerability,
            AnalysisCallbackFinding finding
    ) {
        analysisResultRepository.save(AnalysisResult.createForInfraVulnerability(
                vulnerability,
                defaultText(finding.rootCause(), finding.message(), finding.type()),
                defaultText(finding.summary(), finding.message(), finding.type()),
                finding.impact(),
                resolveFixCode(finding.fixExamples()),
                finding.recommendation()
        ));
    }

    // 코드 취약점 참고 문서 저장
    private void saveSecurityReferences(
            CodeVulnerability vulnerability,
            List<AnalysisCallbackReferenceDocument> referenceDocuments
    ) {
        safeList(referenceDocuments).stream()
                .filter(Objects::nonNull)
                .filter(reference -> StringUtils.hasText(reference.url()) || StringUtils.hasText(reference.title()))
                .map(reference -> SecurityReference.createForCodeVulnerability(
                        vulnerability,
                        resolveReferenceType(reference),
                        limit(reference.title(), 500),
                        limit(reference.url(), 1000)
                ))
                .forEach(securityReferenceRepository::save);
    }

    // 인프라 취약점 참고 문서 저장
    private void saveSecurityReferences(
            InfraVulnerability vulnerability,
            List<AnalysisCallbackReferenceDocument> referenceDocuments
    ) {
        safeList(referenceDocuments).stream()
                .filter(Objects::nonNull)
                .filter(reference -> StringUtils.hasText(reference.url()) || StringUtils.hasText(reference.title()))
                .map(reference -> SecurityReference.createForInfraVulnerability(
                        vulnerability,
                        resolveReferenceType(reference),
                        limit(reference.title(), 500),
                        limit(reference.url(), 1000)
                ))
                .forEach(securityReferenceRepository::save);
    }

    // FastAPI severity 문자열 변환, INFO/알 수 없는 값은 LOW 처리
    private Severity resolveSeverity(String severity) {
        if (!StringUtils.hasText(severity)) {
            log.warn("Unknown severity received from analysis server: {}. Mapping to LOW.", severity);
            return Severity.LOW;
        }

        if ("INFO".equalsIgnoreCase(severity)) {
            return Severity.LOW;
        }

        try {
            return Severity.valueOf(severity.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            log.warn("Unknown severity received from analysis server: {}. Mapping to LOW.", severity);
            return Severity.LOW;
        }
    }

    // sourceType을 ReferenceType으로 변환, 미지원 값은 OTHER 처리
    private ReferenceType resolveReferenceType(AnalysisCallbackReferenceDocument reference) {
        if (!StringUtils.hasText(reference.sourceType())) {
            return ReferenceType.OTHER;
        }

        try {
            return ReferenceType.valueOf(reference.sourceType().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return ReferenceType.OTHER;
        }
    }

    // 첫 번째 fixedCode를 저장용 수정 코드로 선택
    private String resolveFixCode(List<AnalysisCallbackFixExample> fixExamples) {
        return safeList(fixExamples).stream()
                .filter(Objects::nonNull)
                .map(AnalysisCallbackFixExample::fixedCode)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    private String resolveLanguage(List<AnalysisCallbackFixExample> fixExamples) {
        return safeList(fixExamples).stream()
                .filter(Objects::nonNull)
                .map(AnalysisCallbackFixExample::language)
                .filter(StringUtils::hasText)
                .findFirst()
                .map(language -> limit(language, 50))
                .orElse(null);
    }

    private String defaultText(String primary, String secondary, String fallback) {
        if (StringUtils.hasText(primary)) {
            return primary;
        }
        if (StringUtils.hasText(secondary)) {
            return secondary;
        }
        return fallback;
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    // FastAPI 콜백에서 선택 배열 필드가 null로 오더라도 빈 배열처럼 처리
    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }
}

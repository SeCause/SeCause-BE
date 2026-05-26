package SeCause.SeCause_be.domain.analysis.repository;

import SeCause.SeCause_be.domain.analysis.entity.QAnalysis;
import SeCause.SeCause_be.domain.analysis.entity.QAnalysisResult;
import SeCause.SeCause_be.domain.repository.dto.RepositoryIssueDetailResponse;
import SeCause.SeCause_be.domain.repository.dto.RepositoryIssueListResponse;
import SeCause.SeCause_be.domain.repository.dto.RepositoryIssueSummaryResponse;
import SeCause.SeCause_be.domain.repository.dto.SecurityReferenceResponse;
import SeCause.SeCause_be.domain.repository.dto.VulnerableFileListResponse;
import SeCause.SeCause_be.domain.repository.dto.VulnerableFileSummaryResponse;
import SeCause.SeCause_be.domain.repository.entity.FileType;
import SeCause.SeCause_be.domain.repository.entity.QRepository;
import SeCause.SeCause_be.domain.repository.entity.QRepositoryFile;
import SeCause.SeCause_be.domain.security.entity.QSecurityReference;
import SeCause.SeCause_be.domain.vulnerability.entity.QCodeVulnerability;
import SeCause.SeCause_be.domain.vulnerability.entity.QInfraVulnerability;
import SeCause.SeCause_be.domain.vulnerability.entity.Severity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class AnalysisResultRepositoryImpl implements AnalysisResultRepositoryCustom {

    private static final QAnalysisResult analysisResult = QAnalysisResult.analysisResult;
    private static final QCodeVulnerability codeVulnerability = new QCodeVulnerability("codeVulnerability");
    private static final QInfraVulnerability infraVulnerability = new QInfraVulnerability("infraVulnerability");
    private static final QAnalysis codeAnalysis = new QAnalysis("codeAnalysis");
    private static final QAnalysis infraAnalysis = new QAnalysis("infraAnalysis");
    private static final QRepository codeRepository = new QRepository("codeRepository");
    private static final QRepository infraRepository = new QRepository("infraRepository");
    private static final QRepositoryFile codeRepositoryFile = new QRepositoryFile("codeRepositoryFile");
    private static final QRepositoryFile infraRepositoryFile = new QRepositoryFile("infraRepositoryFile");
    private static final QSecurityReference securityReference = QSecurityReference.securityReference;
    private static final NumberExpression<Long> codeIssueCount = codeVulnerability.count();
    private static final NumberExpression<Long> infraIssueCount = infraVulnerability.count();
    private static final NumberExpression<Long> codeCriticalCount =
            codeVulnerability.severity.when(Severity.CRITICAL).then(1L).otherwise(0L).sum();
    private static final NumberExpression<Long> infraCriticalCount =
            infraVulnerability.severity.when(Severity.CRITICAL).then(1L).otherwise(0L).sum();

    private final JPAQueryFactory queryFactory;

    @Override
    public RepositoryIssueListResponse findRepositoryIssues(
            Long repositoryId,
            Long userId,
            Severity severity,
            Pageable pageable
    ) {
        List<Tuple> tuples = baseQuery(repositoryId, userId, severity)
                .select(
                        analysisResult.analysisResultId,
                        codeVulnerability.vulnerabilityType,
                        infraVulnerability.vulnerabilityType,
                        codeVulnerability.severity,
                        infraVulnerability.severity,
                        codeRepositoryFile.filePath,
                        infraRepositoryFile.filePath,
                        codeVulnerability.lineStart,
                        codeVulnerability.lineEnd,
                        analysisResult.summary
                )
                .orderBy(analysisResult.analysisResultId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalElements = baseQuery(repositoryId, userId, severity)
                .select(analysisResult.count())
                .fetchOne();

        return RepositoryIssueListResponse.from(
                tuples.stream().map(this::toResponse).toList(),
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                totalElements == null ? 0 : totalElements
        );
    }

    @Override
    public VulnerableFileListResponse findVulnerableFiles(Long repositoryId, Long userId) {
        Map<Long, VulnerableFileAccumulator> files = new LinkedHashMap<>();
        fetchCodeVulnerableFiles(repositoryId, userId)
                .forEach(tuple -> accumulateCodeVulnerableFile(files, tuple));
        fetchInfraVulnerableFiles(repositoryId, userId)
                .forEach(tuple -> accumulateInfraVulnerableFile(files, tuple));

        return VulnerableFileListResponse.from(
                files.values()
                        .stream()
                        .map(VulnerableFileAccumulator::toResponse)
                        .toList()
        );
    }

    @Override
    public RepositoryIssueDetailResponse findRepositoryIssueDetail(
            Long repositoryId,
            Long userId,
            Long analysisResultId
    ) {
        Tuple tuple = baseQuery(repositoryId, userId, null)
                .select(
                        this.analysisResult.analysisResultId,
                        codeVulnerability.vulnerabilityType,
                        infraVulnerability.vulnerabilityType,
                        codeVulnerability.severity,
                        infraVulnerability.severity,
                        codeRepositoryFile.filePath,
                        infraRepositoryFile.filePath,
                        codeVulnerability.lineStart,
                        codeVulnerability.lineEnd,
                        codeVulnerability.codeSnippet,
                        infraVulnerability.codeSnippet,
                        this.analysisResult.description,
                        this.analysisResult.summary,
                        this.analysisResult.attackScenario,
                        this.analysisResult.fixCode,
                        this.analysisResult.fixSummary
                )
                .where(this.analysisResult.analysisResultId.eq(analysisResultId))
                .fetchOne();

        if (tuple == null) {
            return null;
        }

        Severity codeSeverity = tuple.get(codeVulnerability.severity);
        Severity infraSeverity = tuple.get(infraVulnerability.severity);
        Severity severity = codeSeverity != null ? codeSeverity : infraSeverity;

        return new RepositoryIssueDetailResponse(
                tuple.get(this.analysisResult.analysisResultId),
                firstNonNull(
                        tuple.get(codeVulnerability.vulnerabilityType),
                        tuple.get(infraVulnerability.vulnerabilityType)
                ),
                severity == null ? null : severity.name(),
                firstNonNull(tuple.get(codeRepositoryFile.filePath), tuple.get(infraRepositoryFile.filePath)),
                tuple.get(codeVulnerability.lineStart),
                tuple.get(codeVulnerability.lineEnd),
                firstNonNull(tuple.get(codeVulnerability.codeSnippet), tuple.get(infraVulnerability.codeSnippet)),
                tuple.get(this.analysisResult.description),
                tuple.get(this.analysisResult.summary),
                tuple.get(this.analysisResult.attackScenario),
                tuple.get(this.analysisResult.fixCode),
                tuple.get(this.analysisResult.fixSummary),
                findSecurityReferences(analysisResultId)
        );
    }

    private JPAQuery<?> baseQuery(Long repositoryId, Long userId, Severity severity) {
        return queryFactory
                .from(analysisResult)
                .leftJoin(analysisResult.codeVulnerability, codeVulnerability)
                .leftJoin(analysisResult.infraVulnerability, infraVulnerability)
                .leftJoin(codeVulnerability.analysis, codeAnalysis)
                .leftJoin(infraVulnerability.analysis, infraAnalysis)
                .leftJoin(codeAnalysis.repository, codeRepository)
                .leftJoin(infraAnalysis.repository, infraRepository)
                .leftJoin(codeVulnerability.repositoryFile, codeRepositoryFile)
                .leftJoin(infraVulnerability.repositoryFile, infraRepositoryFile)
                .where(repositoryOwnerCondition(repositoryId, userId), severityCondition(severity));
    }

    private BooleanExpression repositoryOwnerCondition(Long repositoryId, Long userId) {
        return codeRepository.repositoryId.eq(repositoryId)
                .and(codeRepository.user.userId.eq(userId))
                .and(codeRepository.deleted.isFalse())
                .or(infraRepository.repositoryId.eq(repositoryId)
                        .and(infraRepository.user.userId.eq(userId))
                        .and(infraRepository.deleted.isFalse()));
    }

    private BooleanBuilder severityCondition(Severity severity) {
        BooleanBuilder builder = new BooleanBuilder();
        if (severity == null) {
            return builder;
        }

        return builder.and(codeVulnerability.severity.eq(severity)
                .or(infraVulnerability.severity.eq(severity)));
    }

    private RepositoryIssueSummaryResponse toResponse(Tuple tuple) {
        Severity codeSeverity = tuple.get(codeVulnerability.severity);
        Severity infraSeverity = tuple.get(infraVulnerability.severity);
        Severity severity = codeSeverity != null ? codeSeverity : infraSeverity;

        return RepositoryIssueSummaryResponse.of(
                tuple.get(analysisResult.analysisResultId),
                firstNonNull(
                        tuple.get(codeVulnerability.vulnerabilityType),
                        tuple.get(infraVulnerability.vulnerabilityType)
                ),
                severity == null ? null : severity.name(),
                firstNonNull(tuple.get(codeRepositoryFile.filePath), tuple.get(infraRepositoryFile.filePath)),
                tuple.get(codeVulnerability.lineStart),
                tuple.get(codeVulnerability.lineEnd),
                tuple.get(analysisResult.summary)
        );
    }

    private String firstNonNull(String first, String second) {
        return first != null ? first : second;
    }

    private List<SecurityReferenceResponse> findSecurityReferences(Long analysisResultId) {
        QCodeVulnerability referenceCodeVulnerability = new QCodeVulnerability("referenceCodeVulnerability");
        QInfraVulnerability referenceInfraVulnerability = new QInfraVulnerability("referenceInfraVulnerability");
        QAnalysisResult referenceAnalysisResult = new QAnalysisResult("referenceAnalysisResult");

        return queryFactory
                .select(
                        securityReference.securityReferenceId,
                        securityReference.referenceType,
                        securityReference.title,
                        securityReference.referenceUrl
                )
                .from(securityReference)
                .leftJoin(securityReference.codeVulnerability, referenceCodeVulnerability)
                .leftJoin(securityReference.infraVulnerability, referenceInfraVulnerability)
                .leftJoin(referenceAnalysisResult)
                .on(referenceAnalysisResult.codeVulnerability.eq(referenceCodeVulnerability)
                        .or(referenceAnalysisResult.infraVulnerability.eq(referenceInfraVulnerability)))
                .where(referenceAnalysisResult.analysisResultId.eq(analysisResultId))
                .orderBy(securityReference.securityReferenceId.asc())
                .fetch()
                .stream()
                .map(tuple -> new SecurityReferenceResponse(
                        tuple.get(securityReference.securityReferenceId),
                        tuple.get(securityReference.referenceType),
                        tuple.get(securityReference.title),
                        tuple.get(securityReference.referenceUrl)
                ))
                .toList();
    }

    private List<Tuple> fetchCodeVulnerableFiles(Long repositoryId, Long userId) {
        return queryFactory
                .select(
                        codeRepositoryFile.repositoryFileId,
                        codeRepositoryFile.filePath,
                        codeRepositoryFile.fileType,
                        codeRepositoryFile.language,
                        codeIssueCount,
                        codeCriticalCount
                )
                .from(codeVulnerability)
                .join(codeVulnerability.analysis, codeAnalysis)
                .join(codeAnalysis.repository, codeRepository)
                .join(codeVulnerability.repositoryFile, codeRepositoryFile)
                .where(codeRepository.repositoryId.eq(repositoryId),
                        codeRepository.user.userId.eq(userId),
                        codeRepository.deleted.isFalse())
                .groupBy(
                        codeRepositoryFile.repositoryFileId,
                        codeRepositoryFile.filePath,
                        codeRepositoryFile.fileType,
                        codeRepositoryFile.language
                )
                .orderBy(codeRepositoryFile.filePath.asc())
                .fetch();
    }

    private List<Tuple> fetchInfraVulnerableFiles(Long repositoryId, Long userId) {
        return queryFactory
                .select(
                        infraRepositoryFile.repositoryFileId,
                        infraRepositoryFile.filePath,
                        infraRepositoryFile.fileType,
                        infraRepositoryFile.language,
                        infraIssueCount,
                        infraCriticalCount
                )
                .from(infraVulnerability)
                .join(infraVulnerability.analysis, infraAnalysis)
                .join(infraAnalysis.repository, infraRepository)
                .join(infraVulnerability.repositoryFile, infraRepositoryFile)
                .where(infraRepository.repositoryId.eq(repositoryId),
                        infraRepository.user.userId.eq(userId),
                        infraRepository.deleted.isFalse())
                .groupBy(
                        infraRepositoryFile.repositoryFileId,
                        infraRepositoryFile.filePath,
                        infraRepositoryFile.fileType,
                        infraRepositoryFile.language
                )
                .orderBy(infraRepositoryFile.filePath.asc())
                .fetch();
    }

    private void accumulateCodeVulnerableFile(Map<Long, VulnerableFileAccumulator> files, Tuple tuple) {
        Long repositoryFileId = tuple.get(codeRepositoryFile.repositoryFileId);
        VulnerableFileAccumulator accumulator = files.computeIfAbsent(
                repositoryFileId,
                ignored -> VulnerableFileAccumulator.fromCodeTuple(tuple)
        );
        accumulator.addCounts(tuple.get(codeIssueCount), tuple.get(codeCriticalCount));
    }

    private void accumulateInfraVulnerableFile(Map<Long, VulnerableFileAccumulator> files, Tuple tuple) {
        Long repositoryFileId = tuple.get(infraRepositoryFile.repositoryFileId);
        VulnerableFileAccumulator accumulator = files.computeIfAbsent(
                repositoryFileId,
                ignored -> VulnerableFileAccumulator.fromInfraTuple(tuple)
        );
        accumulator.addCounts(tuple.get(infraIssueCount), tuple.get(infraCriticalCount));
    }

    private static class VulnerableFileAccumulator {

        private final Long repositoryFileId;
        private final String filePath;
        private final FileType fileType;
        private final String language;
        private long issueCount;
        private long criticalCount;

        private VulnerableFileAccumulator(
                Long repositoryFileId,
                String filePath,
                FileType fileType,
                String language
        ) {
            this.repositoryFileId = repositoryFileId;
            this.filePath = filePath;
            this.fileType = fileType;
            this.language = language;
        }

        private static VulnerableFileAccumulator fromCodeTuple(Tuple tuple) {
            return new VulnerableFileAccumulator(
                    tuple.get(codeRepositoryFile.repositoryFileId),
                    tuple.get(codeRepositoryFile.filePath),
                    tuple.get(codeRepositoryFile.fileType),
                    tuple.get(codeRepositoryFile.language)
            );
        }

        private static VulnerableFileAccumulator fromInfraTuple(Tuple tuple) {
            return new VulnerableFileAccumulator(
                    tuple.get(infraRepositoryFile.repositoryFileId),
                    tuple.get(infraRepositoryFile.filePath),
                    tuple.get(infraRepositoryFile.fileType),
                    tuple.get(infraRepositoryFile.language)
            );
        }

        private void addCounts(Long issueCount, Long criticalCount) {
            this.issueCount += issueCount == null ? 0 : issueCount;
            this.criticalCount += criticalCount == null ? 0 : criticalCount;
        }

        private VulnerableFileSummaryResponse toResponse() {
            return new VulnerableFileSummaryResponse(
                    repositoryFileId,
                    filePath,
                    fileType,
                    language,
                    issueCount,
                    criticalCount
            );
        }
    }
}

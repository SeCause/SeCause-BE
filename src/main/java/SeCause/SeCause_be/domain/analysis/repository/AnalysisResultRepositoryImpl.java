package SeCause.SeCause_be.domain.analysis.repository;

import SeCause.SeCause_be.domain.analysis.entity.QAnalysis;
import SeCause.SeCause_be.domain.analysis.entity.QAnalysisResult;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryIssueDetailResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryIssueListResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryIssueSummaryResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.SecurityReferenceResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.VulnerableFileListResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.VulnerableFileSummaryResponse;
import SeCause.SeCause_be.domain.projectRepository.entity.FileType;
import SeCause.SeCause_be.domain.projectRepository.entity.QProjectRepository;
import SeCause.SeCause_be.domain.projectRepository.entity.QRepositoryFile;
import SeCause.SeCause_be.domain.security.entity.QSecurityReference;
import SeCause.SeCause_be.domain.vulnerability.entity.QCodeVulnerability;
import SeCause.SeCause_be.domain.vulnerability.entity.QVulnerability;
import SeCause.SeCause_be.domain.vulnerability.entity.Severity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class AnalysisResultRepositoryImpl implements AnalysisResultRepositoryCustom {

    private static final QAnalysisResult analysisResult = QAnalysisResult.analysisResult;
    private static final QVulnerability vulnerability = QVulnerability.vulnerability;
    private static final QCodeVulnerability codeVulnerability = new QCodeVulnerability("codeVulnerability");
    private static final QAnalysis analysis = new QAnalysis("analysis");
    private static final QProjectRepository projectRepository = new QProjectRepository("projectRepository");
    private static final QRepositoryFile repositoryFile = new QRepositoryFile("repositoryFile");
    private static final QSecurityReference securityReference = QSecurityReference.securityReference;

    private static final NumberExpression<Long> issueCount = vulnerability.count();
    private static final NumberExpression<Long> criticalCount =
            vulnerability.severity.when(Severity.CRITICAL).then(1L).otherwise(0L).sum();

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
                        vulnerability.vulnerabilityType,
                        vulnerability.severity,
                        repositoryFile.filePath,
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
                tuples.stream().map(this::toSummaryResponse).toList(),
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                totalElements == null ? 0 : totalElements
        );
    }

    @Override
    public VulnerableFileListResponse findVulnerableFiles(Long repositoryId, Long userId) {
        List<VulnerableFileSummaryResponse> files = queryFactory
                .select(
                        repositoryFile.repositoryFileId,
                        repositoryFile.filePath,
                        repositoryFile.fileType,
                        repositoryFile.language,
                        issueCount,
                        criticalCount
                )
                .from(vulnerability)
                .join(vulnerability.analysis, analysis)
                .join(analysis.repository, projectRepository)
                .join(vulnerability.repositoryFile, repositoryFile)
                .where(repositoryOwnerCondition(repositoryId, userId))
                .groupBy(
                        repositoryFile.repositoryFileId,
                        repositoryFile.filePath,
                        repositoryFile.fileType,
                        repositoryFile.language
                )
                .orderBy(repositoryFile.filePath.asc())
                .fetch()
                .stream()
                .map(this::toVulnerableFileResponse)
                .toList();

        return VulnerableFileListResponse.from(files);
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
                        vulnerability.vulnerabilityType,
                        vulnerability.severity,
                        repositoryFile.filePath,
                        codeVulnerability.lineStart,
                        codeVulnerability.lineEnd,
                        vulnerability.codeSnippet,
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

        Severity severity = tuple.get(vulnerability.severity);

        return new RepositoryIssueDetailResponse(
                tuple.get(this.analysisResult.analysisResultId),
                tuple.get(vulnerability.vulnerabilityType),
                severity == null ? null : severity.name(),
                tuple.get(repositoryFile.filePath),
                tuple.get(codeVulnerability.lineStart),
                tuple.get(codeVulnerability.lineEnd),
                tuple.get(vulnerability.codeSnippet),
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
                .join(analysisResult.vulnerability, vulnerability)
                .join(vulnerability.analysis, analysis)
                .join(analysis.repository, projectRepository)
                .join(vulnerability.repositoryFile, repositoryFile)
                .leftJoin(codeVulnerability)
                .on(codeVulnerability.vulnerabilityId.eq(vulnerability.vulnerabilityId))
                .where(repositoryOwnerCondition(repositoryId, userId), severityCondition(severity));
    }

    private BooleanExpression repositoryOwnerCondition(Long repositoryId, Long userId) {
        return projectRepository.repositoryId.eq(repositoryId)
                .and(projectRepository.user.userId.eq(userId))
                .and(projectRepository.deleted.isFalse());
    }

    private BooleanBuilder severityCondition(Severity severity) {
        BooleanBuilder builder = new BooleanBuilder();
        if (severity == null) {
            return builder;
        }

        return builder.and(vulnerability.severity.eq(severity));
    }

    private RepositoryIssueSummaryResponse toSummaryResponse(Tuple tuple) {
        Severity severity = tuple.get(vulnerability.severity);

        return RepositoryIssueSummaryResponse.of(
                tuple.get(analysisResult.analysisResultId),
                tuple.get(vulnerability.vulnerabilityType),
                severity == null ? null : severity.name(),
                tuple.get(repositoryFile.filePath),
                tuple.get(codeVulnerability.lineStart),
                tuple.get(codeVulnerability.lineEnd),
                tuple.get(analysisResult.summary)
        );
    }

    private VulnerableFileSummaryResponse toVulnerableFileResponse(Tuple tuple) {
        Long fileIssueCount = tuple.get(issueCount);
        Long fileCriticalCount = tuple.get(criticalCount);
        FileType fileType = tuple.get(repositoryFile.fileType);

        return new VulnerableFileSummaryResponse(
                tuple.get(repositoryFile.repositoryFileId),
                tuple.get(repositoryFile.filePath),
                fileType,
                tuple.get(repositoryFile.language),
                fileIssueCount == null ? 0 : fileIssueCount,
                fileCriticalCount == null ? 0 : fileCriticalCount
        );
    }

    private List<SecurityReferenceResponse> findSecurityReferences(Long analysisResultId) {
        QAnalysisResult referenceAnalysisResult = new QAnalysisResult("referenceAnalysisResult");
        QVulnerability referenceVulnerability = new QVulnerability("referenceVulnerability");

        return queryFactory
                .select(
                        securityReference.securityReferenceId,
                        securityReference.referenceType,
                        securityReference.title,
                        securityReference.referenceUrl
                )
                .from(referenceAnalysisResult)
                .join(referenceAnalysisResult.vulnerability, referenceVulnerability)
                .join(securityReference)
                .on(securityReference.vulnerability.eq(referenceVulnerability))
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
}

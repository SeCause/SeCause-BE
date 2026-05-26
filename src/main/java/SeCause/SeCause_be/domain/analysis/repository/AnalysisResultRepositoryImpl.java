package SeCause.SeCause_be.domain.analysis.repository;

import SeCause.SeCause_be.domain.analysis.entity.QAnalysis;
import SeCause.SeCause_be.domain.analysis.entity.QAnalysisResult;
import SeCause.SeCause_be.domain.repository.dto.RepositoryIssueListResponse;
import SeCause.SeCause_be.domain.repository.dto.RepositoryIssueSummaryResponse;
import SeCause.SeCause_be.domain.repository.entity.QRepository;
import SeCause.SeCause_be.domain.repository.entity.QRepositoryFile;
import SeCause.SeCause_be.domain.vulnerability.entity.QCodeVulnerability;
import SeCause.SeCause_be.domain.vulnerability.entity.QInfraVulnerability;
import SeCause.SeCause_be.domain.vulnerability.entity.Severity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

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
}

package SeCause.SeCause_be.domain.projectRepository.repository;

import SeCause.SeCause_be.domain.analysis.entity.QAnalysis;
import SeCause.SeCause_be.domain.analysis.entity.QAnalysisResult;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositorySeverityCountResponse;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositorySummaryResponse;
import SeCause.SeCause_be.domain.projectRepository.entity.QProjectRepository;
import SeCause.SeCause_be.domain.projectRepository.entity.QRepositoryFile;
import SeCause.SeCause_be.domain.vulnerability.entity.QVulnerability;
import SeCause.SeCause_be.domain.vulnerability.entity.Severity;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class ProjectRepositoryRepositoryImpl implements ProjectRepositoryRepositoryCustom {

    private static final QProjectRepository projectRepository = QProjectRepository.projectRepository;
    private static final QAnalysis analysis = QAnalysis.analysis;
    private static final QAnalysisResult analysisResult = QAnalysisResult.analysisResult;
    private static final QRepositoryFile repositoryFile = QRepositoryFile.repositoryFile;
    private static final QVulnerability vulnerability = QVulnerability.vulnerability;
    private static final NumberExpression<Long> issueCount = analysisResult.count();

    private final JPAQueryFactory queryFactory;

    @Override
    public List<RepositorySummaryResponse> findRepositorySummaries(
            Long userId,
            String accountName,
            String keyword
    ) {
        List<Tuple> repositories = queryFactory
                .select(
                        projectRepository.repositoryId,
                        projectRepository.owner,
                        projectRepository.title,
                        projectRepository.branch,
                        projectRepository.totalFiles,
                        projectRepository.lineCount,
                        analysis.analysisStatus,
                        analysis.progressPercent,
                        analysis.createdAt,
                        analysis.completedAt
                )
                .from(projectRepository)
                .join(analysis).on(analysis.repository.eq(projectRepository))
                .where(repositoryCondition(userId, accountName, keyword))
                .orderBy(analysis.createdAt.desc(), projectRepository.repositoryId.desc())
                .fetch();

        Map<Long, List<String>> languagesByRepository = findLanguagesByRepository(
                extractRepositoryIds(repositories)
        );
        Map<Long, Map<Severity, Long>> issueCountsByRepository = findIssueCountsByRepository(
                extractRepositoryIds(repositories)
        );

        return repositories.stream()
                .map(tuple -> toSummary(tuple, languagesByRepository, issueCountsByRepository))
                .toList();
    }

    @Override
    public Optional<RepositoryDashboardQueryResult> findRepositoryDashboard(
            Long repositoryId,
            Long userId
    ) {
        Tuple repository = queryFactory
                .select(
                        projectRepository.repositoryId,
                        projectRepository.owner,
                        projectRepository.title,
                        projectRepository.description,
                        projectRepository.githubLink,
                        projectRepository.branch,
                        projectRepository.totalFiles,
                        projectRepository.lineCount,
                        analysis.analysisStatus,
                        analysis.progressPercent,
                        analysis.createdAt,
                        analysis.completedAt,
                        analysis.failureReason
                )
                .from(projectRepository)
                .join(analysis).on(analysis.repository.eq(projectRepository))
                .where(repositoryOwnerCondition(repositoryId, userId))
                .fetchOne();

        if (repository == null) {
            return Optional.empty();
        }

        List<String> languages = findLanguagesByRepository(List.of(repositoryId))
                .getOrDefault(repositoryId, List.of());

        return Optional.of(new RepositoryDashboardQueryResult(
                repositoryId,
                repository.get(projectRepository.owner),
                repository.get(projectRepository.title),
                repository.get(projectRepository.description),
                repository.get(projectRepository.githubLink),
                repository.get(projectRepository.branch),
                valueOrZero(repository.get(projectRepository.totalFiles)),
                valueOrZero(repository.get(projectRepository.lineCount)),
                languages,
                repository.get(analysis.analysisStatus),
                valueOrZero(repository.get(analysis.progressPercent)),
                repository.get(analysis.createdAt),
                repository.get(analysis.completedAt),
                repository.get(analysis.failureReason),
                findIssueCountsByType(repositoryId),
                findIssueCountsBySeverity(repositoryId)
        ));
    }

    private BooleanBuilder repositoryCondition(Long userId, String accountName, String keyword) {
        BooleanBuilder condition = new BooleanBuilder()
                .and(projectRepository.user.userId.eq(userId))
                .and(projectRepository.deleted.isFalse());

        if (StringUtils.hasText(accountName)) {
            condition.and(projectRepository.owner.equalsIgnoreCase(accountName.trim()));
        }

        if (StringUtils.hasText(keyword)) {
            String normalizedKeyword = keyword.trim();
            condition.and(
                    projectRepository.title.containsIgnoreCase(normalizedKeyword)
                            .or(projectRepository.owner.containsIgnoreCase(normalizedKeyword))
            );
        }

        return condition;
    }

    private BooleanExpression repositoryOwnerCondition(Long repositoryId, Long userId) {
        return projectRepository.repositoryId.eq(repositoryId)
                .and(projectRepository.user.userId.eq(userId))
                .and(projectRepository.deleted.isFalse());
    }

    private Map<Long, List<String>> findLanguagesByRepository(List<Long> repositoryIds) {
        if (repositoryIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, List<String>> languagesByRepository = new LinkedHashMap<>();

        queryFactory
                .select(repositoryFile.repository.repositoryId, repositoryFile.language)
                .distinct()
                .from(repositoryFile)
                .where(
                        repositoryFile.repository.repositoryId.in(repositoryIds),
                        repositoryFile.language.isNotNull(),
                        repositoryFile.language.isNotEmpty()
                )
                .orderBy(
                        repositoryFile.repository.repositoryId.asc(),
                        repositoryFile.language.asc()
                )
                .fetch()
                .forEach(tuple -> languagesByRepository
                        .computeIfAbsent(
                                tuple.get(repositoryFile.repository.repositoryId),
                                ignored -> new ArrayList<>()
                        )
                        .add(tuple.get(repositoryFile.language)));

        return languagesByRepository;
    }

    private Map<Long, Map<Severity, Long>> findIssueCountsByRepository(List<Long> repositoryIds) {
        if (repositoryIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, Map<Severity, Long>> issueCountsByRepository = new LinkedHashMap<>();

        queryFactory
                .select(
                        analysis.repository.repositoryId,
                        vulnerability.severity,
                        issueCount
                )
                .from(analysisResult)
                .join(analysisResult.vulnerability, vulnerability)
                .join(vulnerability.analysis, analysis)
                .where(analysis.repository.repositoryId.in(repositoryIds))
                .groupBy(analysis.repository.repositoryId, vulnerability.severity)
                .fetch()
                .forEach(tuple -> issueCountsByRepository
                        .computeIfAbsent(
                                tuple.get(analysis.repository.repositoryId),
                                ignored -> new EnumMap<>(Severity.class)
                        )
                        .put(
                                tuple.get(vulnerability.severity),
                                valueOrZero(tuple.get(issueCount))
                        ));

        return issueCountsByRepository;
    }

    private List<RepositoryDashboardQueryResult.IssueTypeCount> findIssueCountsByType(Long repositoryId) {
        return queryFactory
                .select(
                        vulnerability.vulnerabilityType,
                        vulnerability.severity,
                        issueCount
                )
                .from(analysisResult)
                .join(analysisResult.vulnerability, vulnerability)
                .join(vulnerability.analysis, analysis)
                .where(analysis.repository.repositoryId.eq(repositoryId))
                .groupBy(vulnerability.vulnerabilityType, vulnerability.severity)
                .orderBy(
                        vulnerability.severity.asc(),
                        issueCount.desc(),
                        vulnerability.vulnerabilityType.asc()
                )
                .fetch()
                .stream()
                .map(tuple -> new RepositoryDashboardQueryResult.IssueTypeCount(
                        tuple.get(vulnerability.vulnerabilityType),
                        tuple.get(vulnerability.severity),
                        valueOrZero(tuple.get(issueCount))
                ))
                .toList();
    }

    private Map<Severity, Long> findIssueCountsBySeverity(Long repositoryId) {
        Map<Severity, Long> countsBySeverity = new EnumMap<>(Severity.class);

        queryFactory
                .select(vulnerability.severity, issueCount)
                .from(analysisResult)
                .join(analysisResult.vulnerability, vulnerability)
                .join(vulnerability.analysis, analysis)
                .where(analysis.repository.repositoryId.eq(repositoryId))
                .groupBy(vulnerability.severity)
                .fetch()
                .forEach(tuple -> countsBySeverity.put(
                        tuple.get(vulnerability.severity),
                        valueOrZero(tuple.get(issueCount))
                ));

        return countsBySeverity;
    }

    private RepositorySummaryResponse toSummary(
            Tuple tuple,
            Map<Long, List<String>> languagesByRepository,
            Map<Long, Map<Severity, Long>> issueCountsByRepository
    ) {
        Long repositoryId = tuple.get(projectRepository.repositoryId);
        Map<Severity, Long> issueCounts = issueCountsByRepository.getOrDefault(
                repositoryId,
                Collections.emptyMap()
        );

        return RepositorySummaryResponse.of(
                repositoryId,
                tuple.get(projectRepository.owner),
                tuple.get(projectRepository.title),
                tuple.get(projectRepository.branch),
                valueOrZero(tuple.get(projectRepository.totalFiles)),
                valueOrZero(tuple.get(projectRepository.lineCount)),
                languagesByRepository.getOrDefault(repositoryId, List.of()),
                new RepositorySeverityCountResponse(
                        issueCounts.getOrDefault(Severity.CRITICAL, 0L),
                        issueCounts.getOrDefault(Severity.HIGH, 0L),
                        issueCounts.getOrDefault(Severity.MEDIUM, 0L),
                        issueCounts.getOrDefault(Severity.LOW, 0L)
                ),
                tuple.get(analysis.analysisStatus),
                valueOrZero(tuple.get(analysis.progressPercent)),
                tuple.get(analysis.createdAt),
                tuple.get(analysis.completedAt)
        );
    }

    private List<Long> extractRepositoryIds(List<Tuple> repositories) {
        return repositories.stream()
                .map(tuple -> tuple.get(projectRepository.repositoryId))
                .toList();
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }
}

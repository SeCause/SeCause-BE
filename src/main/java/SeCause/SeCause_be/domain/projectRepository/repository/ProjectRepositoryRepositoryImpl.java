package SeCause.SeCause_be.domain.projectRepository.repository;

import SeCause.SeCause_be.domain.analysis.entity.QAnalysis;
import SeCause.SeCause_be.domain.projectRepository.dto.RepositorySummaryResponse;
import SeCause.SeCause_be.domain.projectRepository.entity.QProjectRepository;
import SeCause.SeCause_be.domain.projectRepository.entity.QRepositoryFile;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ProjectRepositoryRepositoryImpl implements ProjectRepositoryRepositoryCustom {

    private static final QProjectRepository projectRepository = QProjectRepository.projectRepository;
    private static final QAnalysis analysis = QAnalysis.analysis;
    private static final QRepositoryFile repositoryFile = QRepositoryFile.repositoryFile;

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
                repositories.stream()
                        .map(tuple -> tuple.get(projectRepository.repositoryId))
                        .toList()
        );

        return repositories.stream()
                .map(tuple -> toSummary(tuple, languagesByRepository))
                .toList();
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

    private RepositorySummaryResponse toSummary(
            Tuple tuple,
            Map<Long, List<String>> languagesByRepository
    ) {
        Long repositoryId = tuple.get(projectRepository.repositoryId);

        return RepositorySummaryResponse.of(
                repositoryId,
                tuple.get(projectRepository.owner),
                tuple.get(projectRepository.title),
                tuple.get(projectRepository.branch),
                valueOrZero(tuple.get(projectRepository.totalFiles)),
                valueOrZero(tuple.get(projectRepository.lineCount)),
                languagesByRepository.getOrDefault(repositoryId, List.of()),
                tuple.get(analysis.analysisStatus),
                valueOrZero(tuple.get(analysis.progressPercent)),
                tuple.get(analysis.createdAt),
                tuple.get(analysis.completedAt)
        );
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }
}

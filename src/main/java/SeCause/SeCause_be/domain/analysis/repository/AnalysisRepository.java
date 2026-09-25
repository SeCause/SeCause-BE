package SeCause.SeCause_be.domain.analysis.repository;

import SeCause.SeCause_be.domain.analysis.entity.Analysis;
import SeCause.SeCause_be.domain.analysis.entity.AnalysisStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

    boolean existsByRepositoryGithubLinkAndRepositoryBranchAndRepositoryUserUserIdAndRepositoryDeletedFalseAndAnalysisStatusIn(
            String githubLink,
            String branch,
            Long userId,
            Collection<AnalysisStatus> analysisStatuses
    );

    @EntityGraph(attributePaths = "repository")
    Optional<Analysis> findWithRepositoryByAnalysisId(Long analysisId);

    @EntityGraph(attributePaths = {"repository", "repository.user"})
    Optional<Analysis> findWithRepositoryAndUserByAnalysisId(Long analysisId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "repository")
    @Query("select a from Analysis a where a.analysisId = :analysisId")
    Optional<Analysis> findForUpdateWithRepositoryByAnalysisId(@Param("analysisId") Long analysisId);
}

package SeCause.SeCause_be.domain.analysis.repository;

import SeCause.SeCause_be.domain.analysis.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
}

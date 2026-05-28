package SeCause.SeCause_be.domain.projectRepository.repository;

import SeCause.SeCause_be.domain.projectRepository.entity.ProjectRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepositoryRepository extends JpaRepository<ProjectRepository, Long> {

    boolean existsByRepositoryIdAndUserUserIdAndDeletedFalse(Long repositoryId, Long userId);
}

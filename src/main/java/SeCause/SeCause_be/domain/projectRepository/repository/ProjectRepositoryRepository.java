package SeCause.SeCause_be.domain.projectRepository.repository;

import SeCause.SeCause_be.domain.projectRepository.entity.ProjectRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepositoryRepository extends
        JpaRepository<ProjectRepository, Long>,
        ProjectRepositoryRepositoryCustom {

    /**
     * 로그인 사용자가 소유한 삭제되지 않은 레포지토리인지 확인합니다.
     */
    boolean existsByRepositoryIdAndUserUserIdAndDeletedFalse(Long repositoryId, Long userId);
}

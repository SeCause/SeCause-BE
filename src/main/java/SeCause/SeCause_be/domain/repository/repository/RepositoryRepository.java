package SeCause.SeCause_be.domain.repository.repository;

import SeCause.SeCause_be.domain.repository.entity.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositoryRepository extends JpaRepository<Repository, Long> {

    boolean existsByRepositoryIdAndUserUserIdAndDeletedFalse(Long repositoryId, Long userId);
}

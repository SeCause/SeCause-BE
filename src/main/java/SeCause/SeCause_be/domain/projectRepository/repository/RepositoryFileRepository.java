package SeCause.SeCause_be.domain.projectRepository.repository;

import SeCause.SeCause_be.domain.projectRepository.entity.RepositoryFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RepositoryFileRepository extends JpaRepository<RepositoryFile, Long> {

    List<RepositoryFile> findAllByRepositoryRepositoryIdAndFilePathIn(
            Long repositoryId,
            Collection<String> filePaths
    );
}

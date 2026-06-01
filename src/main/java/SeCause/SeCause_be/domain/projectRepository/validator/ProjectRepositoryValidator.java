package SeCause.SeCause_be.domain.projectRepository.validator;

import SeCause.SeCause_be.domain.projectRepository.code.ProjectRepositoryErrorCode;
import SeCause.SeCause_be.domain.projectRepository.exception.ProjectRepositoryException;
import SeCause.SeCause_be.domain.projectRepository.repository.ProjectRepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectRepositoryValidator {

    private final ProjectRepositoryRepository projectRepositoryRepository;

    public void validateRepositoryOwner(Long repositoryId, Long userId) {
        boolean exists = projectRepositoryRepository.existsByRepositoryIdAndUserUserIdAndDeletedFalse(
                repositoryId,
                userId
        );
        if (!exists) {
            throw new ProjectRepositoryException(ProjectRepositoryErrorCode.PROJECT_REPOSITORY_NOT_FOUND);
        }
    }
}

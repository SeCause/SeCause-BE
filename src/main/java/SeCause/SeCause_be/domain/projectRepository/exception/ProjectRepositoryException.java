package SeCause.SeCause_be.domain.projectRepository.exception;

import SeCause.SeCause_be.domain.projectRepository.exception.code.ProjectRepositoryErrorCode;
import SeCause.SeCause_be.global.apiPayload.exception.GeneralException;

public class ProjectRepositoryException extends GeneralException {

    public ProjectRepositoryException(ProjectRepositoryErrorCode errorCode) {
        super(errorCode);
    }
}

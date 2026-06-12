package SeCause.SeCause_be.domain.projectRepository.service;

import SeCause.SeCause_be.domain.projectRepository.dto.RepositoryListResponse;
import SeCause.SeCause_be.domain.projectRepository.repository.ProjectRepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectRepositoryService {

    private final ProjectRepositoryRepository projectRepositoryRepository;

    /**
     * 로그인 사용자가 분석한 레포지토리 목록을 조회합니다.
     */
    public RepositoryListResponse getRepositories(Long userId, String accountName, String keyword) {
        return RepositoryListResponse.from(
                projectRepositoryRepository.findRepositorySummaries(userId, accountName, keyword)
        );
    }
}

package SeCause.SeCause_be.domain.projectRepository.repository;

import SeCause.SeCause_be.domain.projectRepository.dto.RepositorySummaryResponse;

import java.util.List;

public interface ProjectRepositoryRepositoryCustom {

    /**
     * 로그인 사용자의 레포지토리 목록을 계정명과 검색어로 필터링하여 조회합니다.
     */
    List<RepositorySummaryResponse> findRepositorySummaries(
            Long userId,
            String accountName,
            String keyword
    );
}

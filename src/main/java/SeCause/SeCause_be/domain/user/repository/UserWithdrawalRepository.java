package SeCause.SeCause_be.domain.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class UserWithdrawalRepository {

    private static final String OWNED_REPOSITORIES = """
            SELECT repository_id FROM repositories WHERE user_id = :userId
            """;
    private static final String OWNED_ANALYSES = """
            SELECT analysis_id FROM analyses WHERE repository_id IN (%s)
            """.formatted(OWNED_REPOSITORIES);
    private static final String OWNED_VULNERABILITIES = """
            SELECT vulnerability_id FROM vulnerabilities WHERE analysis_id IN (%s)
            """.formatted(OWNED_ANALYSES);

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.MANDATORY)
    public void deleteOwnedData(Long userId) {
        Map<String, Long> parameters = Map.of("userId", userId);

        jdbcTemplate.queryForList("""
                SELECT a.analysis_id
                FROM analyses a
                JOIN repositories r ON r.repository_id = a.repository_id
                WHERE r.user_id = :userId
                ORDER BY a.analysis_id
                FOR UPDATE OF a, r
                """, parameters, Long.class);

        delete("analysis_results", "vulnerability_id", OWNED_VULNERABILITIES, parameters);
        delete("security_references", "vulnerability_id", OWNED_VULNERABILITIES, parameters);
        // JOINED 상속의 자식 테이블을 먼저 지워서 부모 취약점의 외래 키 제약을 지킴
        delete("code_vulnerabilities", "code_vulnerability_id", OWNED_VULNERABILITIES, parameters);
        delete("infra_vulnerabilities", "infra_vulnerability_id", OWNED_VULNERABILITIES, parameters);
        delete("vulnerabilities", "analysis_id", OWNED_ANALYSES, parameters);
        delete("repository_files", "repository_id", OWNED_REPOSITORIES, parameters);
        delete("analyses", "repository_id", OWNED_REPOSITORIES, parameters);
        jdbcTemplate.update("DELETE FROM repositories WHERE user_id = :userId", parameters);
    }

    private void delete(String table, String column, String ownedIds, Map<String, Long> parameters) {
        jdbcTemplate.update("DELETE FROM %s WHERE %s IN (%s)".formatted(table, column, ownedIds), parameters);
    }
}

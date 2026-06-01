package SeCause.SeCause_be.domain.projectRepository.dto;

import SeCause.SeCause_be.domain.vulnerability.entity.Severity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RepositoryIssueSeverity {
    ALL("전체"),
    CRITICAL("치명"),
    HIGH("높음"),
    MEDIUM("보통"),
    LOW("낮음"),
    ;

    private final String description;

    public Severity toSeverityOrNull() {
        if (this == ALL) {
            return null;
        }

        return Severity.valueOf(name());
    }
}

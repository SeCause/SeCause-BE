package SeCause.SeCause_be.domain.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LinkableGithubAccountType {
    PERSONAL("계정"),
    ORGANIZATION("조직")
    ;

    final String description;
}

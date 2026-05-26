package SeCause.SeCause_be.domain.security.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReferenceType {
    OWASP("OWASP 보안 문서"),
    CWE("CWE 취약점 분류"),
    OTHER("기타 보안 자료"),
    ;

    private final String description;
}

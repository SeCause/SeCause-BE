package SeCause.SeCause_be.domain.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileType {
    SOURCE("소스 코드 파일"),
    INFRA("인프라 설정 파일"),
    OTHER("기타 파일"),
    ;

    private final String description;
}

package SeCause.SeCause_be.domain.analysis.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AnalysisStatus {
    PENDING("분석 대기"),
    IN_PROGRESS("분석 진행 중"),
    COMPLETED("분석 완료"),
    FAILED("분석 실패"),
    CANCELLED("분석 취소"),
    ;

    private final String description;
}

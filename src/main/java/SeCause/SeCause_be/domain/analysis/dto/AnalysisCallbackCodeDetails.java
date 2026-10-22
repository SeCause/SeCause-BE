package SeCause.SeCause_be.domain.analysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "전체 분석 대상 코드 통계. 객체 제공 시 모든 필드가 필수입니다.")
public record AnalysisCallbackCodeDetails(
        @Schema(description = "취약점 유무와 무관한 전체 분석 대상 파일 수", example = "42")
        @NotNull @PositiveOrZero Integer fileCount,
        @Schema(description = "전체 분석 대상 파일의 라인 수", example = "1000")
        @NotNull @PositiveOrZero Long lineCount,
        @Schema(description = "전체 분석 대상 파일의 사용 언어 목록. 빈 배열 허용")
        @NotNull List<@NotBlank @Size(max = 100) String> languages
) {
}

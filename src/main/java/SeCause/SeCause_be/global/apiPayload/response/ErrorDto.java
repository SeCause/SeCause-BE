package SeCause.SeCause_be.global.apiPayload.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDto {

    private final String reason;
    private final Map<String, String> validation;

    public static ErrorDto of(String reason) {
        return new ErrorDto(reason, null);
    }

    public static ErrorDto of(Map<String, String> validation) {
        return new ErrorDto(null, validation);
    }
}

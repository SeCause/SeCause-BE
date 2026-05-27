package SeCause.SeCause_be.global.apiPayload.response;

import SeCause.SeCause_be.global.apiPayload.code.BaseErrorCode;
import SeCause.SeCause_be.global.apiPayload.code.BaseSuccessCode;
import SeCause.SeCause_be.global.apiPayload.code.GlobalSuccessCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"isSuccess", "code", "message", "result", "error"})
public record ApiResponse<T>(
        @JsonProperty("isSuccess") boolean isSuccess,
        @JsonProperty("code") String code,
        @JsonProperty("message") String message,
        @JsonProperty("result") T result,
        @JsonProperty("error") ErrorDto error) {


    public static <T> ApiResponse<T> onSuccess(String message, T result) {
        return new ApiResponse<>(true, GlobalSuccessCode.OK.getCode(), message, result, null);
    }

    public static <T> ApiResponse<T> onSuccess(String message) {
        return new ApiResponse<>(true, GlobalSuccessCode.OK.getCode(), message, null, null);
    }

    public static <T> ApiResponse<T> onSuccess(BaseSuccessCode successCode, T result) {
        return new ApiResponse<>(true, successCode.getCode(), successCode.getMessage(), result, null);
    }

    public static <T> ApiResponse<T> onSuccess(BaseSuccessCode successCode) {
        return new ApiResponse<>(true, successCode.getCode(), successCode.getMessage(), null, null);
    }


    public static <T> ApiResponse<T> onFailure(BaseErrorCode errorCode, ErrorDto error) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null, error);
    }
}

package SeCause.SeCause_be.global.apiPayload.exception.handler;

import SeCause.SeCause_be.global.apiPayload.code.BaseErrorCode;
import SeCause.SeCause_be.global.apiPayload.code.GlobalErrorCode;
import SeCause.SeCause_be.global.apiPayload.exception.GeneralException;
import SeCause.SeCause_be.global.apiPayload.response.ApiResponse;
import SeCause.SeCause_be.global.apiPayload.response.ErrorDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(GeneralException exception) {
        BaseErrorCode errorCode = exception.getErrorCode();
        log.error("GeneralException: {}", exception.getErrorCode().getMessage());
        return handleExceptionInternal(errorCode, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> validation = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors()
                .forEach(error -> validation.put(error.getField(), error.getDefaultMessage()));

        log.warn("Validation failed: {}", validation);

        return handleExceptionInternal(GlobalErrorCode.VALIDATION_ERROR, ErrorDto.of(validation));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception
    ) {
        return handleExceptionInternal(
                GlobalErrorCode.INVALID_REQUEST_BODY,
                null
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception
    ) {
        String reason = exception.getParameterName() + " 파라미터가 필요합니다.";
        return handleExceptionInternal(GlobalErrorCode.MISSING_REQUEST_PARAMETER, ErrorDto.of(reason));
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingPathVariableException(
            MissingPathVariableException exception
    ) {
        String reason = exception.getVariableName() + " 경로 변수가 필요합니다.";
        return handleExceptionInternal(GlobalErrorCode.MISSING_REQUEST_PARAMETER, ErrorDto.of(reason));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception
    ) {
        String reason = exception.getName() + " 값의 타입이 올바르지 않습니다.";
        return handleExceptionInternal(GlobalErrorCode.TYPE_MISMATCH, ErrorDto.of(reason));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException exception
    ) {
        return handleExceptionInternal(GlobalErrorCode.METHOD_NOT_ALLOWED, ErrorDto.of(exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception exception, HttpServletRequest request) {

        log.error("Unhandled exception occurred. URI: {} {}",
                request.getMethod(), request.getRequestURI(), exception);

        return handleExceptionInternal(
                GlobalErrorCode.INTERNAL_SERVER_ERROR,
                null
        );
    }

    private ResponseEntity<ApiResponse<Void>> handleExceptionInternal(BaseErrorCode errorCode, ErrorDto error) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.onFailure(errorCode, error));
    }
}

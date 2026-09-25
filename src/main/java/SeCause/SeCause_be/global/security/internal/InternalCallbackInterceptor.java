package SeCause.SeCause_be.global.security.internal;

import SeCause.SeCause_be.domain.analysis.properties.AnalysisCallbackProperties;
import SeCause.SeCause_be.global.apiPayload.code.GlobalErrorCode;
import SeCause.SeCause_be.global.apiPayload.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class InternalCallbackInterceptor implements HandlerInterceptor {

    public static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private final AnalysisCallbackProperties analysisCallbackProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {
        String internalToken = analysisCallbackProperties.internalToken();
        String requestToken = request.getHeader(INTERNAL_TOKEN_HEADER);
        if (internalToken.equals(requestToken)) {
            return true;
        }

        response.setStatus(GlobalErrorCode.UNAUTHORIZED.getStatus().value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getWriter(),
                ApiResponse.onFailure(GlobalErrorCode.UNAUTHORIZED, null)
        );
        return false;
    }
}

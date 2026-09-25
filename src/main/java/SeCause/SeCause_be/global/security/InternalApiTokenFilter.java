package SeCause.SeCause_be.global.security;

import SeCause.SeCause_be.global.apiPayload.code.GlobalErrorCode;
import SeCause.SeCause_be.global.apiPayload.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class InternalApiTokenFilter extends OncePerRequestFilter {

    private static final String INTERNAL_PATH_PREFIX = "/api/internal/";
    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    private final ObjectMapper objectMapper;
    private final byte[] expectedToken;

    public InternalApiTokenFilter(
            ObjectMapper objectMapper,
            @Value("${internal.api.token}") String internalApiToken
    ) {
        this.objectMapper = objectMapper;
        this.expectedToken = internalApiToken.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(INTERNAL_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String providedToken = request.getHeader(INTERNAL_TOKEN_HEADER);
        if (!matches(providedToken)) {
            writeUnauthorized(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean matches(String providedToken) {
        return providedToken != null && MessageDigest.isEqual(
                expectedToken,
                providedToken.getBytes(StandardCharsets.UTF_8)
        );
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(GlobalErrorCode.UNAUTHORIZED.getStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.onFailure(GlobalErrorCode.UNAUTHORIZED, null));
    }
}

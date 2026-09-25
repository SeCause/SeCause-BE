package SeCause.SeCause_be.global.security.internal;

import SeCause.SeCause_be.domain.analysis.properties.AnalysisCallbackProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class InternalCallbackInterceptorTest {

    private final InternalCallbackInterceptor interceptor = new InternalCallbackInterceptor(
            new AnalysisCallbackProperties("shared-secret")
    );

    @Test
    void rejectsInvalidInternalToken() throws Exception {
        MockHttpServletRequest request = request("wrong-secret");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"isSuccess\":false", "\"code\":\"COMMON401\"");
    }

    @Test
    void acceptsValidInternalToken() throws Exception {
        MockHttpServletRequest request = request("shared-secret");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request, response, new Object());

        assertThat(allowed).isTrue();
    }

    private MockHttpServletRequest request(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                "/api/internal/analyses/1/result"
        );
        request.addHeader(InternalCallbackInterceptor.INTERNAL_TOKEN_HEADER, token);
        return request;
    }
}

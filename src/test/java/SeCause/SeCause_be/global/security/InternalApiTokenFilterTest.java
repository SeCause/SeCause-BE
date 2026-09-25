package SeCause.SeCause_be.global.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class InternalApiTokenFilterTest {

    private final InternalApiTokenFilter filter = new InternalApiTokenFilter("shared-secret");

    @Test
    void rejectsInvalidTokenOnInternalPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                "/api/internal/analyses/1/result"
        );
        request.addHeader("X-Internal-Token", "wrong-secret");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"isSuccess\":false", "\"code\":\"COMMON401\"");
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void acceptsValidTokenOnInternalPath() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                "/api/internal/analyses/1/failure"
        );
        request.addHeader("X-Internal-Token", "shared-secret");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}

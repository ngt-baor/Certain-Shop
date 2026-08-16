package com.certainshop.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitFilterTest {

    @Test
    void blocksThirdAuthenticationAttemptWithinWindow() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(2, 60, 3, 60);

        assertEquals(200, invoke(filter, "POST", "/api/auth/dang-nhap", "127.0.0.1").getStatus());
        assertEquals(200, invoke(filter, "POST", "/api/auth/dang-nhap", "127.0.0.1").getStatus());

        MockHttpServletResponse blocked = invoke(filter, "POST", "/api/auth/dang-nhap", "127.0.0.1");
        assertEquals(429, blocked.getStatus());
        assertEquals("60", blocked.getHeader("Retry-After"));
    }

    @Test
    void usesSeparateLimitsPerClientAndEndpointGroup() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(1, 60, 1, 60);

        assertEquals(200, invoke(filter, "POST", "/api/auth/dang-ky", "10.0.0.1").getStatus());
        assertEquals(429, invoke(filter, "POST", "/api/auth/dang-ky", "10.0.0.1").getStatus());
        assertEquals(200, invoke(filter, "POST", "/api/auth/dang-ky", "10.0.0.2").getStatus());
        assertEquals(200, invoke(filter, "GET", "/api/dia-chi/tinh-thanh", "10.0.0.1").getStatus());
        assertEquals(429, invoke(filter, "GET", "/api/dia-chi/tinh-thanh", "10.0.0.1").getStatus());
    }

    @Test
    void doesNotLimitUnrelatedAuthenticatedApis() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(1, 60, 1, 60);
        for (int i = 0; i < 5; i++) {
            assertEquals(200, invoke(filter, "GET", "/api/gio-hang", "127.0.0.1").getStatus());
        }
    }

    private MockHttpServletResponse invoke(RateLimitFilter filter, String method, String uri, String remoteAddress)
            throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr(remoteAddress);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}

package com.certainshop.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter extends OncePerRequestFilter {

    private final int authMaxAttempts;
    private final long authWindowMillis;
    private final int publicMaxRequests;
    private final long publicWindowMillis;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final AtomicLong requestCounter = new AtomicLong();

    public RateLimitFilter(
            @Value("${app.rate-limit.auth.max-attempts:10}") int authMaxAttempts,
            @Value("${app.rate-limit.auth.window-seconds:60}") long authWindowSeconds,
            @Value("${app.rate-limit.public.max-requests:120}") int publicMaxRequests,
            @Value("${app.rate-limit.public.window-seconds:60}") long publicWindowSeconds) {
        this.authMaxAttempts = authMaxAttempts;
        this.authWindowMillis = authWindowSeconds * 1_000;
        this.publicMaxRequests = publicMaxRequests;
        this.publicWindowMillis = publicWindowSeconds * 1_000;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String group = routeGroup(request);
        if (group == null) {
            filterChain.doFilter(request, response);
            return;
        }

        long now = System.currentTimeMillis();
        boolean auth = "auth".equals(group);
        int limit = auth ? authMaxAttempts : publicMaxRequests;
        long windowMillis = auth ? authWindowMillis : publicWindowMillis;
        String key = group + ':' + request.getRemoteAddr();
        Window window = windows.computeIfAbsent(key, ignored -> new Window(now));

        if (!window.tryAcquire(now, limit, windowMillis)) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(Math.max(1, windowMillis / 1_000)));
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().write("{\"thanhCong\":false,\"thongBao\":\"Quá nhiều yêu cầu, vui lòng thử lại sau\",\"maLoi\":429}");
            return;
        }

        if ((requestCounter.incrementAndGet() & 255) == 0) {
            windows.entrySet().removeIf(entry -> entry.getValue().isExpired(now, windowMillis));
        }
        filterChain.doFilter(request, response);
    }

    private String routeGroup(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if ("POST".equals(request.getMethod())
                && ("/api/auth/dang-nhap".equals(uri) || "/api/auth/dang-ky".equals(uri))) {
            return "auth";
        }
        if (uri.startsWith("/api/ghn/")
                || uri.startsWith("/api/dia-chi/tinh-thanh")
                || uri.startsWith("/api/dia-chi/quan-huyen")
                || uri.startsWith("/api/dia-chi/phuong-xa")) {
            return "public";
        }
        return null;
    }

    private static final class Window {
        private long startedAt;
        private int count;

        private Window(long startedAt) {
            this.startedAt = startedAt;
        }

        private synchronized boolean tryAcquire(long now, int limit, long windowMillis) {
            if (now - startedAt >= windowMillis) {
                startedAt = now;
                count = 0;
            }
            if (count >= limit) return false;
            count++;
            return true;
        }

        private synchronized boolean isExpired(long now, long windowMillis) {
            return now - startedAt >= windowMillis;
        }
    }
}

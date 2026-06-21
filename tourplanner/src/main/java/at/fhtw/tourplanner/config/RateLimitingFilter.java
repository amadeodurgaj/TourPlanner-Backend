package at.fhtw.tourplanner.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(1)
public class RateLimitingFilter implements Filter {

    private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 5;
    private static final long WINDOW_MILLIS = 60_000;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        String ip = servletRequest.getRemoteAddr();
        String path = servletRequest instanceof jakarta.servlet.http.HttpServletRequest req
                ? req.getRequestURI() : "";

        if (path.equals("/api/auth/login") || path.equals("/api/register")) {
            RateLimiter limiter = rateLimiters.computeIfAbsent(ip, k -> new RateLimiter(MAX_REQUESTS, WINDOW_MILLIS));
            if (!limiter.tryAcquire()) {
                HttpServletResponse response = (HttpServletResponse) servletResponse;
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Too many requests. Please try again later.\"}");
                return;
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private static class RateLimiter {
        private final int maxRequests;
        private final long windowMillis;
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart = System.currentTimeMillis();

        RateLimiter(int maxRequests, long windowMillis) {
            this.maxRequests = maxRequests;
            this.windowMillis = windowMillis;
        }

        synchronized boolean tryAcquire() {
            long now = System.currentTimeMillis();
            if (now - windowStart > windowMillis) {
                windowStart = now;
                count.set(0);
            }
            return count.incrementAndGet() <= maxRequests;
        }
    }
}

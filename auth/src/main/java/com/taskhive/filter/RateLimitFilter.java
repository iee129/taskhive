package com.taskhive.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskhive.exception.ErrorCode;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Bucket resolveBucket(String ip, String uri) {
        String key = ip + ":" + getBucketCategory(uri);
        return buckets.computeIfAbsent(key, k -> createBucket(uri));
    }

    private String getBucketCategory(String uri) {
        if (uri.startsWith("/auth/login")) return "login";
        if (uri.startsWith("/auth/register")) return "register";
        if (uri.startsWith("/auth/forgot-password") || uri.startsWith("/auth/reset-password")) return "password";
        return "default";
    }

    private Bucket createBucket(String uri) {
        int capacity;
        if (uri.startsWith("/auth/login")) {
            capacity = 10;
        } else if (uri.startsWith("/auth/register")) {
            capacity = 5;
        } else if (uri.startsWith("/auth/forgot-password") || uri.startsWith("/auth/reset-password")) {
            capacity = 3;
        } else {
            capacity = 100;
        }
        return Bucket.builder()
                .addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofMinutes(1))))
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        String uri = request.getRequestURI();

        Bucket bucket = resolveBucket(ip, uri);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            ErrorCode errorCode = ErrorCode.RATE_LIMIT_EXCEEDED;
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.addHeader("X-RateLimit-Remaining", "0");
            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            Map.of("code", errorCode.name(), "message", errorCode.getMessage())
                    )
            );
        }
    }
}

package com.iotmining.services.notification.filter;

import com.iotmining.services.notification.model.Plan;
import com.iotmining.services.notification.model.Priority;
import com.iotmining.services.notification.ratelimiter.Bucket4jRateLimiterService;
import com.iotmining.services.notification.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationRateLimitFilter extends OncePerRequestFilter {

    private final Bucket4jRateLimiterService rateLimiterService;

    private static final String PUBLIC_URI   = "/api/notifications/send";
    private static final String INTERNAL_URI = "/api/notifications/internal/send";
    private static final String EXPECTED_AUDIENCE = "notification-service";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain)
            throws ServletException, IOException {

        final String uri = request.getRequestURI();

        // ===== INTERNAL (pre-registration OTP)
        if (INTERNAL_URI.equals(uri)) {
            log.debug("Processing INTERNAL notification request. URI: {}", uri);

            String authHeader = Optional.ofNullable(request.getHeader("Authorization"))
                    .orElse(request.getHeader("authorization"));
            if (authHeader == null || !authHeader.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
                reject(response, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
                return;
            }

            final Jws<Claims> jws;
            try {
                jws = JwtUtil.validateToken(authHeader.substring(7));
            } catch (JwtException e) {
                log.warn("Invalid internal JWT: {}", e.getMessage());
                reject(response, HttpStatus.UNAUTHORIZED, "Invalid JWT: " + e.getMessage());
                return;
            }

            Claims claims = jws.getBody();

            // scope=internal
            boolean scopeOk = false;
            Object scope = claims.get("scope");
            if (scope instanceof String s) {
                scopeOk = "internal".equalsIgnoreCase(s.trim());
            } else if (scope instanceof Iterable<?> it) {
                for (Object o : it) {
                    if (o != null && "internal".equalsIgnoreCase(String.valueOf(o))) {
                        scopeOk = true; break;
                    }
                }
            }
            if (!scopeOk) {
                reject(response, HttpStatus.FORBIDDEN, "JWT missing required scope=internal");
                return;
            }

            // audience
            String aud = Optional.ofNullable(claims.get("aud", String.class))
                    .orElse(claims.getAudience());
            if (aud == null || !EXPECTED_AUDIENCE.equalsIgnoreCase(aud)) {
                reject(response, HttpStatus.FORBIDDEN, "Invalid JWT audience");
                return;
            }

            // prospectId (claim or header)
            String prospectIdStr = Optional.ofNullable(claims.get("prospectId", String.class))
                    .orElse(request.getHeader("X-Prospect-ID"));
            if (prospectIdStr == null) {
                reject(response, HttpStatus.BAD_REQUEST, "Missing prospectId (claim or X-Prospect-ID header)");
                return;
            }

            final UUID prospectId;
            try {
                prospectId = UUID.fromString(prospectIdStr);
            } catch (IllegalArgumentException e) {
                reject(response, HttpStatus.BAD_REQUEST, "Invalid UUID for prospectId");
                return;
            }

            MDC.put("prospectId", prospectId.toString());
            try {
                rateLimiterService.executeWithRateLimit(
                        prospectId.toString(), Plan.BASIC, Priority.HIGH, () -> null
                );
            } catch (Exception ex) {
                log.warn("INTERNAL rate limit exceeded for prospect {}: {}", prospectId, ex.getMessage());
                reject(response, HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded: " + ex.getMessage());
                return;
            } finally {
                MDC.remove("prospectId");
            }

            filterChain.doFilter(request, response);
            return;
        }

        // ===== PUBLIC (post-registration)
        if (PUBLIC_URI.equals(uri)) {
            String userIdStr = request.getHeader("X-User-ID");
            log.debug("Processing PUBLIC notification request. URI: {}", uri);

            if (userIdStr == null) {
                String authHeader = Optional.ofNullable(request.getHeader("Authorization"))
                        .orElse(request.getHeader("authorization"));
                if (authHeader != null && authHeader.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
                    try {
                        Jws<Claims> claims = JwtUtil.validateToken(authHeader.substring(7));
                        userIdStr = claims.getBody().get("userId", String.class);
                        log.debug("User ID extracted from JWT: {}", userIdStr);
                    } catch (JwtException e) {
                        log.warn("Invalid JWT during user ID extraction: {}", e.getMessage());
                        reject(response, HttpStatus.UNAUTHORIZED, "Invalid JWT: " + e.getMessage());
                        return;
                    }
                }
            }

            if (userIdStr == null) {
                reject(response, HttpStatus.BAD_REQUEST, "Missing X-User-ID header or valid Bearer token");
                return;
            }

            final UUID userId;
            try {
                userId = UUID.fromString(userIdStr);
            } catch (IllegalArgumentException e) {
                reject(response, HttpStatus.BAD_REQUEST, "Invalid UUID format for userId: " + userIdStr);
                return;
            }

            try {
                rateLimiterService.executeWithRateLimit(
                        userId.toString(), Plan.BASIC, Priority.STANDARD, () -> null
                );
            } catch (Exception ex) {
                log.warn("Rate limit exceeded for user {}: {}", userId, ex.getMessage());
                reject(response, HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded: " + ex.getMessage());
                return;
            }
        } else {
            log.trace("Skipping rate limit filter for URI: {}", uri);
        }

        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try (PrintWriter writer = response.getWriter()) {
            writer.write("{\"error\": \"" + message + "\"}");
            writer.flush();
        }
        log.info("Request rejected with status {} and message: {}", status.value(), message);
    }
}

//package com.iotmining.services.notification.filter;
//
//import com.iotmining.services.notification.model.Plan;
//import com.iotmining.services.notification.model.Priority;
//import com.iotmining.services.notification.ratelimiter.Bucket4jRateLimiterService;
//import com.iotmining.services.notification.utils.JwtUtil;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jws;
//import io.jsonwebtoken.JwtException;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.UUID;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class NotificationRateLimitFilter extends OncePerRequestFilter {
//
//    private final Bucket4jRateLimiterService rateLimiterService;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    @NotNull HttpServletResponse response,
//                                    @NotNull FilterChain filterChain)
//            throws ServletException, IOException {
//
//        if (request.getRequestURI().equals("/api/notifications/send")) {
//            String userIdStr = request.getHeader("X-User-ID");
//            // Log the start of the rate limit filter processing for a notification request
//            log.debug("Processing rate limit for notification send request. Request URI: {}", request.getRequestURI());
//
//            // Optional: Try token fallback if no header
//            if (userIdStr == null) {
//                String authHeader = request.getHeader("Authorization");
//                if (authHeader != null && authHeader.startsWith("Bearer ")) {
//                    String token = authHeader.substring(7);
//                    try {
//                        Jws<Claims> claims = JwtUtil.validateToken(token);
//                        userIdStr = claims.getBody().get("userId", String.class);
//                        // Log successful extraction of userId from JWT
//                        log.debug("User ID extracted from JWT: {}", userIdStr);
//                    } catch (JwtException e) {
//                        // Log the specific JWT error
//                        log.warn("Invalid JWT provided during user ID extraction: {}", e.getMessage());
//                        reject(response, HttpStatus.UNAUTHORIZED, "Invalid JWT: " + e.getMessage());
//                        return;
//                    }
//                } else {
//                    // Log that Authorization header was present but not a Bearer token
//                    log.debug("Authorization header present but not a Bearer token or null.");
//                }
//            } else {
//                // Log that X-User-ID header was found
//                log.debug("X-User-ID header found: {}", userIdStr);
//            }
//
//
//            if (userIdStr == null) {
//                // Log the reason for rejection due to missing user ID
//                log.warn("Request rejected: Missing X-User-ID header or valid Bearer token for URI: {}", request.getRequestURI());
//                reject(response, HttpStatus.BAD_REQUEST, "Missing X-User-ID header or valid Bearer token");
//                return;
//            }
//
//            UUID userId;
//            try {
//                userId = UUID.fromString(userIdStr);
//                // Log successful parsing of UUID
//                log.debug("Successfully parsed userId UUID: {}", userId);
//            } catch (IllegalArgumentException e) {
//                // Log the invalid UUID format
//                log.warn("Request rejected: Invalid UUID format for userId '{}'", userIdStr);
//                reject(response, HttpStatus.BAD_REQUEST, "Invalid UUID format for userId: " + userIdStr);
//                return;
//            }
//
//            try {
//                // Basic plan + STANDARD priority (can be improved based on token)
//                // Log the user and the plan/priority being used for rate limiting
//                log.debug("Applying rate limit for user {} with Plan: {} and Priority: {}", userId, Plan.BASIC, Priority.STANDARD);
//                rateLimiterService.executeWithRateLimit(
//                        userId.toString(), Plan.BASIC, Priority.STANDARD, () -> null
//                );
//                // Log successful rate limit execution
//                log.debug("Rate limit check passed for user {}", userId);
//            } catch (Exception ex) {
//                // Original log is good, but adding more context from the exception message.
//                log.warn("Rate limit exceeded for user {}: {}", userId, ex.getMessage());
//                reject(response, HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded: " + ex.getMessage());
//                return;
//            }
//        } else {
//            // Log if the request URI is not subject to rate limiting by this filter
//            log.trace("Skipping rate limit filter for URI: {}", request.getRequestURI());
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    private void reject(HttpServletResponse response, HttpStatus status, String message) throws IOException {
//        response.setStatus(status.value());
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//        PrintWriter writer = response.getWriter();
//        writer.write("{\"error\": \"" + message + "\"}");
//        writer.flush();
//        // Log the rejection details
//        log.info("Request rejected with status {} and message: {}", status.value(), message);
//    }
//}
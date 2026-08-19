package com.iotmining.services.notification.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;

/**
 * Validates the internal service-to-service JWT minted by
 * JwtTokenProvider.issueInternalToken (auth-service) for callers of
 * /api/notifications/internal/send. Same signing key, scope/aud claim
 * shape, and raw-claim-plus-instanceof-Collection reading pattern (JJWT
 * throws instead of converting for some claim shapes via the typed
 * claims.get(key, Class) getter) already used by telemetry-stream-service's
 * filter of the same name.
 *
 * The expected audience is NOT derived from spring.application.name here -
 * this service's spring.application.name is "IoTMining-Notification-Service"
 * (mixed case, a naming-convention outlier on this platform), but
 * issueInternalToken's default overload mints tokens with aud="notification-service"
 * literally. Deriving from spring.application.name would reject every
 * legitimate internal token.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${internal.jwt.audience:notification-service}")
    private String expectedAudience;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // "aud" is a registered JWT claim the spec allows as either a single
            // string or an array of strings - JJWT represents it internally as a
            // Set regardless of cardinality, so claims.get("aud", String.class)
            // throws rather than converts. Read the raw claim and check its
            // runtime type instead.
            Object scopeClaim = claims.get("scope");
            Object audienceClaim = claims.get("aud");
            boolean isInternalScope = "internal".equals(scopeClaim);
            boolean audienceMatches = audienceClaim instanceof java.util.Collection<?> audiences
                    ? audiences.contains(expectedAudience)
                    : expectedAudience.equals(audienceClaim);

            if (isInternalScope && audienceMatches) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        claims, null, List.of(new SimpleGrantedAuthority("SCOPE_INTERNAL"))
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (Exception e) {
            log.warn("Invalid JWT in notification-service: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}

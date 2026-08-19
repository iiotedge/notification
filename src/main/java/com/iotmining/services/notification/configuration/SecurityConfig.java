package com.iotmining.services.notification.configuration;

import com.iotmining.services.notification.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. DISABLE CSRF (Required for React/Axios POST requests)
                .csrf(csrf -> csrf.disable())

                // 2. DISABLE CORS (Required because Nginx handles this)
                .cors(cors -> cors.disable())

                // 3. /api/notifications/internal/send accepts a prospectId/userId
                //    supplied entirely by the caller and dispatches a real
                //    email/SMS on their behalf - it was reachable with zero
                //    auth (any request could send a notification as any
                //    prospect/user). Now requires the same internal
                //    service-to-service JWT (scope=internal, aud=this
                //    service) that auth-service already mints and sends for
                //    every OTP - JwtAuthenticationFilter grants SCOPE_INTERNAL
                //    when that checks out. Everything else keeps its
                //    pre-existing open access (Nginx/network is still the
                //    boundary for those) - not widening this pass beyond the
                //    endpoint actually implicated in the OTP flow.
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/notifications/internal/send").hasAuthority("SCOPE_INTERNAL")
                        .requestMatchers("/**").permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

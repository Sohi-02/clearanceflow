
package com.clearanceflow.backend.config;

import jakarta.servlet.DispatcherType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;

@Configuration
public class SecurityConfig {

    // SECURITY FILTER CHAIN
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // Allow Spring Boot to display error responses
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                        // Public endpoints
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        // Only applicants can create companies
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/companies"
                        ).hasRole("APPLICANT")

                        // Only officers can view the complete company list
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/companies"
                        ).hasRole("OFFICER")

                        // Only applicants can update companies
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/companies/*"
                        ).hasRole("APPLICANT")

                        // Only applicants can delete companies
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/companies/*"
                        ).hasRole("APPLICANT")

                        // Applicants and officers can request an individual company
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/companies/*"
                        ).hasAnyRole("APPLICANT", "OFFICER")

                        // Require authentication for any remaining endpoints
                        .anyRequest().authenticated()
                )

                // Validate JWT Bearer tokens
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                jwtAuthenticationConverter()
                        ))
                );

        return http.build();
    }

    // BCRYPT PASSWORD HASHING
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // JWT DECODER
    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${app.jwt.secret}") String encodedSecret) {

        byte[] secretBytes = Base64.getDecoder().decode(encodedSecret);

        if (secretBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must contain at least 32 bytes"
            );
        }

        SecretKeySpec key =
                new SecretKeySpec(secretBytes, "HmacSHA256");

        return NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    // CONVERT JWT ROLE CLAIM INTO SPRING SECURITY AUTHORITY
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            String role = jwt.getClaimAsString("role");

            if (role == null ||
                    (!role.equals("APPLICANT") &&
                            !role.equals("OFFICER"))) {

                return List.of();
            }

            return List.of(
                    new SimpleGrantedAuthority("ROLE_" + role)
            );
        });

        return converter;
    }
}

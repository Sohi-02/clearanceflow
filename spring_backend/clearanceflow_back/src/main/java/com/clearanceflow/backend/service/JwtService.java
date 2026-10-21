
package com.clearanceflow.backend.service;

import com.clearanceflow.backend.entity.User;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    private final byte[] secret;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String encodedSecret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {

        this.secret = Base64.getDecoder().decode(encodedSecret);

        if (secret.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must contain at least 32 bytes");
        }

        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        try {
            Instant now = Instant.now();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getEmail())
                    .claim("role", user.getRole().name())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(
                            now.plusMillis(expirationMs)))
                    .build();

            SignedJWT jwt = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claims
            );

            jwt.sign(new MACSigner(secret));

            return jwt.serialize();

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to generate JWT", ex);
        }
    }
}

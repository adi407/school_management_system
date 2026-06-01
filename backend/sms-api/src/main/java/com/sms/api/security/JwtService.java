package com.sms.api.security;

import com.sms.core.enums.FeatureKey;
import com.sms.core.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * JWT access tokens use HMAC-SHA256 (HS256) for simplicity.
 * Tokens embed userId, schoolId, role, and enabled feature keys
 * so the frontend can gate UI without an extra round-trip.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey signingKey;
    private final long accessExpiryMs;
    private final long refreshExpiryMs;

    public JwtService(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.access-expiry-minutes:15}") long accessMinutes,
        @Value("${jwt.refresh-expiry-days:7}") long refreshDays
    ) {
        this.signingKey = Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8)
        );
        this.accessExpiryMs  = accessMinutes * 60 * 1000L;
        this.refreshExpiryMs = refreshDays * 24 * 60 * 60 * 1000L;
    }

    public String generateAccessToken(UUID userId, UUID schoolId, Role role, List<FeatureKey> features, String email) {
        return Jwts.builder()
            .subject(userId.toString())
            .claim("schoolId", schoolId != null ? schoolId.toString() : null)
            .claim("role", role.name())
            .claim("email", email)
            .claim("features", features.stream().map(Enum::name).toList())
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusMillis(accessExpiryMs)))
            .signWith(signingKey)
            .compact();
    }

    public String generateRefreshToken(UUID userId) {
        return Jwts.builder()
            .subject(userId.toString())
            .claim("type", "refresh")
            .issuedAt(Date.from(Instant.now()))
            .expiration(Date.from(Instant.now().plusMillis(refreshExpiryMs)))
            .signWith(signingKey)
            .compact();
    }

    public Claims validateAndExtract(String token) {
        try {
            return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            throw new JwtException("Invalid or expired token", e);
        }
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(validateAndExtract(token).getSubject());
    }

    public long getRefreshExpiryMs() { return refreshExpiryMs; }
}

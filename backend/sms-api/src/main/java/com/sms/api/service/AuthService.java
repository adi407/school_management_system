package com.sms.api.service;

import com.sms.api.dto.auth.AuthResponse;
import com.sms.api.dto.auth.LoginRequest;
import com.sms.api.dto.auth.RefreshRequest;
import com.sms.api.entity.FeatureFlag;
import com.sms.api.entity.PasswordResetToken;
import com.sms.api.entity.RefreshToken;
import com.sms.api.entity.User;
import com.sms.api.repository.FeatureFlagRepository;
import com.sms.api.repository.PasswordResetTokenRepository;
import com.sms.api.repository.RefreshTokenRepository;
import com.sms.api.repository.UserRepository;
import com.sms.api.security.JwtService;
import com.sms.core.enums.FeatureKey;
import com.sms.api.dto.auth.ForgotPasswordRequest;
import com.sms.api.dto.auth.ResetPasswordRequest;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final FeatureFlagRepository featureFlagRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
        UserRepository userRepository,
        RefreshTokenRepository refreshTokenRepository,
        FeatureFlagRepository featureFlagRepository,
        PasswordResetTokenRepository passwordResetTokenRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.userRepository       = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.featureFlagRepository = featureFlagRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder      = passwordEncoder;
        this.jwtService           = jwtService;
    }

    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new BadCredentialsException("Account is deactivated");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        user.setLastLoginAt(Instant.now());

        return buildAuthResponse(user, ipAddress, userAgent);
    }

    public AuthResponse refresh(RefreshRequest request) {
        String tokenHash = hashToken(request.refreshToken());
        RefreshToken rt = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new JwtException("Refresh token not found"));

        if (rt.isRevoked() || rt.isExpired()) {
            throw new JwtException("Refresh token is invalid or expired");
        }

        // Rotate: revoke old, issue new
        rt.setRevokedAt(Instant.now());
        return buildAuthResponse(rt.getUser(), null, null);
    }

    public void logout(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId, Instant.now());
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request, String ipAddress, String userAgent) {
        // Avoid user enumeration: always respond without revealing whether the email exists.
        userRepository.findByEmail(request.email()).ifPresentOrElse(user -> {
            // Generate opaque reset token; store only its hash in DB.
            String token = generateRandomToken();
            String tokenHash = hashToken(token);

            PasswordResetToken prt = new PasswordResetToken();
            prt.setUser(user);
            prt.setTokenHash(tokenHash);
            prt.setExpiresAt(Instant.now().plusSeconds(30 * 60)); // 30 minutes
            passwordResetTokenRepository.save(prt);

            // Dev fallback: log the token instead of sending email.
            // In production, replace this with JavaMailSender integration.
            // (We keep IP/UA params in case you later want to store them on the token record.)
            org.slf4j.LoggerFactory.getLogger(AuthService.class).info(
                "Password reset token for {} (dev): {} (ip={}, ua={})",
                user.getEmail(), token, ipAddress, userAgent
            );
        }, () -> {
            // Intentionally no-op.
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = hashToken(request.token());

        PasswordResetToken prt = passwordResetTokenRepository.findByTokenHashAndUsedAtIsNull(tokenHash)
            .orElseThrow(() -> new BadCredentialsException("Invalid or expired reset token"));

        if (prt.isExpired() || prt.isUsed()) {
            throw new BadCredentialsException("Invalid or expired reset token");
        }

        User user = prt.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        prt.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(prt);
    }

    private AuthResponse buildAuthResponse(User user, String ipAddress, String userAgent) {
        UUID schoolId = user.getSchool() != null ? user.getSchool().getId() : null;

        List<FeatureKey> features = schoolId != null
            ? featureFlagRepository.findEnabledBySchoolId(schoolId).stream()
                .map(FeatureFlag::getFeatureKey).toList()
            : List.of(FeatureKey.values()); // SUPER_ADMIN gets all

        String accessToken  = jwtService.generateAccessToken(user.getId(), schoolId, user.getRole(), features, user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        // Store hashed refresh token
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(hashToken(refreshToken));
        rt.setIpAddress(ipAddress);
        rt.setDeviceInfo(userAgent);
        rt.setExpiresAt(Instant.now().plusMillis(jwtService.getRefreshExpiryMs()));
        refreshTokenRepository.save(rt);

        String fullName   = user.getSchool() != null ? user.getEmail() : "Super Admin";
        String schoolName = user.getSchool() != null ? user.getSchool().getName() : null;

        return new AuthResponse(
            accessToken,
            refreshToken,
            new AuthResponse.UserInfo(
                user.getId(),
                schoolId,
                user.getEmail(),
                fullName,
                user.getRole(),
                user.getProfilePhotoUrl(),
                features.stream().map(Enum::name).toList(),
                schoolName
            )
        );
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private String generateRandomToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

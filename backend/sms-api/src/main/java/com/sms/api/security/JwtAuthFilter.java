package com.sms.api.security;

import com.sms.core.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.persistence.EntityManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final EntityManager entityManager;

    public JwtAuthFilter(JwtService jwtService, EntityManager entityManager) {
        this.jwtService = jwtService;
        this.entityManager = entityManager;
    }

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        try {
            Claims claims = jwtService.validateAndExtract(token);
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UUID userId        = UUID.fromString(claims.getSubject());
                String schoolIdStr = claims.get("schoolId", String.class);
                UUID schoolId      = schoolIdStr != null ? UUID.fromString(schoolIdStr) : null;
                Role role          = Role.valueOf(claims.get("role", String.class));
                String email       = claims.get("email", String.class);

                // Enable Hibernate tenant filter so every school-scoped entity query is automatically
                // restricted to the current schoolId (critical for preventing cross-tenant reads).
                if (schoolId != null) {
                    Session session = entityManager.unwrap(Session.class);
                    session.enableFilter("schoolFilter")
                        .setParameter("schoolId", schoolId);
                }

                // Fall back to sub (userId) if email claim is missing (old tokens)
                String resolvedEmail = (email != null) ? email : claims.getSubject();
                UserPrincipal principal = new UserPrincipal(userId, schoolId, resolvedEmail, role);
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (JwtException e) {
            // Invalid token — don't set authentication; Spring Security will handle 401
        }

        filterChain.doFilter(request, response);
    }
}

package org.oppexai.util;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.oppexai.model.User;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class JwtUtil {

    private static final String ISSUER = "oppex-ai";
    private static final Duration TOKEN_EXPIRY = Duration.ofDays(30);

    public String generateToken(User user) {
        Set<String> roles = new HashSet<>();
        roles.add("user");

        return Jwt.issuer(ISSUER)
                .upn(user.getEmail())
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("isVerified", user.getIsVerified())
                .groups(roles)
                .expiresIn(TOKEN_EXPIRY)
                .sign();
    }

    public String generateToken(String email, Long userId, Boolean isVerified) {
        Set<String> roles = new HashSet<>();
        roles.add("user");

        return Jwt.issuer(ISSUER)
                .upn(email)
                .claim("userId", userId)
                .claim("email", email)
                .claim("isVerified", isVerified)
                .groups(roles)
                .expiresIn(TOKEN_EXPIRY)
                .sign();
    }

    public String generateToken(String email) {
        Set<String> roles = new HashSet<>();
        roles.add("user");

        return Jwt.issuer(ISSUER)
                .upn(email)
                .claim("email", email)
                .groups(roles)
                .expiresIn(TOKEN_EXPIRY)
                .sign();
    }

}
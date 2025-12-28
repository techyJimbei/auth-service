package org.oppexai.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;

import org.jboss.logging.Logger;
import org.oppexai.model.User;
import org.oppexai.repository.UserRepository;
import org.oppexai.util.JwtUtil;
import org.oppexai.util.PasswordUtil;
import java.util.Base64;

@ApplicationScoped
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class);

    // Dummy hash for constant-time comparison
    private static final String DUMMY_PASSWORD_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    @Inject
    UserRepository userRepository;

    @Inject
    JwtUtil jwtUtil;

    @Inject
    UserService userService;

    /**
     * Authenticate user with constant-time verification
     */
    public String login(String email, String plainPassword) {
        LOG.infof("Login attempt for email: %s", email);

        // Fetch user from database
        User user = userRepository.findByEmail(email).orElse(null);

        // Prepare hash to verify against (constant-time)
        String hashToVerify = (user != null) ? user.getPasswordHash() : DUMMY_PASSWORD_HASH;

        // ALWAYS perform BCrypt verification (prevents timing attacks)
        boolean passwordMatches = PasswordUtil.verifyPassword(plainPassword, hashToVerify);

        // Check if user exists AND password matches
        if (user == null || !passwordMatches) {
            LOG.warnf("Login failed for email: %s", email);
            throw new NotAuthorizedException("Invalid email or password");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user);
        LOG.infof("Login successful for: %s", email);

        return token;
    }

    /**
     * Extract email from JWT token in Authorization header
     * @param authHeader Authorization header value (Bearer token)
     * @return Email from token
     */
    public String extractEmail(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new NotAuthorizedException("Invalid authorization header");
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        try {
            // Parse JWT token (Quarkus will validate it automatically)
            // For now, just extract the email claim
            // In production, use proper JWT parsing with public key verification
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new NotAuthorizedException("Invalid token format");
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

            // Simple JSON parsing to extract email
            // In production, use a proper JSON library
            String email = payload.split("\"email\":\"")[1].split("\"")[0];

            LOG.infof("Extracted email from token: %s", email);
            return email;

        } catch (Exception e) {
            LOG.errorf("Token parsing failed: %s", e.getMessage());
            throw new NotAuthorizedException("Invalid token");
        }
    }

    public User getUserFromToken(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotAuthorizedException("Invalid token"));
    }
}
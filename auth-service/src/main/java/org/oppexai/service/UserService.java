package org.oppexai.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;
import org.oppexai.model.User;
import org.oppexai.repository.UserRepository;
import org.oppexai.util.PasswordUtil;

import java.util.UUID;

@ApplicationScoped
public class UserService {

    private static final Logger LOG = Logger.getLogger(UserService.class);

    @Inject
    UserRepository userRepository;

    @Inject
    EmailService emailService;


    public void signup(String email, String password) {
        LOG.infof("Starting signup process for: %s", email);

        String verificationToken = createNewUser(email, password);

        try {
            emailService.sendVerificationEmail(email, verificationToken);
        } catch (Exception e) {
            LOG.errorf("User saved but email failed for %s: %s", email, e.getMessage());
        }
    }

    @Transactional
    protected String createNewUser(String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }

        String verificationToken = UUID.randomUUID().toString();
        User user = new User(email, PasswordUtil.hashPassword(password));
        user.setIsVerified(false);
        user.setVerificationToken(verificationToken);

        userRepository.persist(user);
        LOG.infof("User persisted to Supabase: %s", email);
        return verificationToken;
    }

    @Transactional
    public void verifyEmail(String token) {
        LOG.infof("Attempting to verify email with token: %s", token);

        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> {
                    LOG.warnf("Verification failed: Invalid token - %s", token);
                    return new NotFoundException("Invalid or expired verification token");
                });

        if (user.getIsVerified()) {
            LOG.infof("Email already verified for user: %s", user.getEmail());
            return;
        }

        user.setIsVerified(true);
        user.setVerificationToken(null);

        userRepository.persist(user);

        LOG.infof("Email verified successfully for user: %s", user.getEmail());
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    LOG.warnf("User not found with email: %s", email);
                    return new NotFoundException("User not found");
                });
    }


    public User findById(Long id) {
        return userRepository.findByIdOptional(id)
                .orElseThrow(() -> {
                    LOG.warnf("User not found with ID: %s", id);
                    return new NotFoundException("User not found");
                });
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isUserVerified(String email) {
        User user = findByEmail(email);
        return user.getIsVerified();
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        LOG.infof("Attempting to resend verification email to: %s", email);

        User user = findByEmail(email);

        if (user.getIsVerified()) {
            LOG.warnf("Cannot resend verification: User already verified - %s", email);
            throw new BadRequestException("Email already verified");
        }

        String verificationToken = generateVerificationToken();
        user.setVerificationToken(verificationToken);

        userRepository.persist(user);

        emailService.sendVerificationEmail(email, verificationToken);

        LOG.infof("Verification email resent to: %s", email);
    }


    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }
}
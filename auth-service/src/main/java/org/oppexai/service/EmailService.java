package org.oppexai.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EmailService {

    private static final Logger LOG = Logger.getLogger(EmailService.class);

    @Inject
    Mailer mailer;

    public void sendVerificationEmail(String email, String verificationToken) {
        String verificationLink = "http://localhost:3000/verify/" + verificationToken;

        String subject = "Verify Your Email - Oppex AI";
        String body = String.format("""
            Hello,
            
            Thank you for signing up!
            
            Please click the link below to verify your email address:
            %s
            
            If you didn't create an account, please ignore this email.
            
            Best regards,
            Oppex AI Team
            """, verificationLink);

        try {
            mailer.send(Mail.withText(email, subject, body));
            LOG.infof("Verification email sent to: %s", email);
            LOG.infof("Verification link: %s", verificationLink);
        } catch (Exception e) {
            LOG.errorf("Failed to send verification email to %s: %s", email, e.getMessage());
        }
    }

}
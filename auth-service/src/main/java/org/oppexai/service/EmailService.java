package org.oppexai.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EmailService {

    private static final Logger LOG = Logger.getLogger(EmailService.class);

    @Inject
    Mailer mailer;

    @ConfigProperty(name = "app.backend.url", defaultValue = "http://localhost:8080")
    String backendUrl;

    @ConfigProperty(name = "app.frontend.url", defaultValue = "http://localhost:3000")
    String frontendUrl;

    public void sendVerificationEmail(String email, String verificationToken) {
        String verificationLink = backendUrl + "/api/auth/verify?token=" + verificationToken;

        String subject = "Verify Your Email - Oppex AI";

        String htmlBody = String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); 
                         color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                .content { background: #f9fafb; padding: 30px; border-radius: 0 0 8px 8px; }
                .button { display: inline-block; background: #667eea; color: white; 
                         padding: 12px 30px; text-decoration: none; border-radius: 6px; 
                         font-weight: bold; margin: 20px 0; }
                .footer { text-align: center; margin-top: 20px; color: #6b7280; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Welcome to Oppex AI!</h1>
                </div>
                <div class="content">
                    <h2>Verify Your Email Address</h2>
                    <p>Thank you for signing up! Please verify your email address by clicking the button below:</p>
                    <div style="text-align: center;">
                        <a href="%s" class="button">Verify Email Address</a>
                    </div>
                    <p>Or copy and paste this link into your browser:</p>
                    <p style="word-break: break-all; color: #667eea;">%s</p>
                    <p style="margin-top: 20px; color: #6b7280; font-size: 14px;">
                        If you didn't create an account, please ignore this email.
                    </p>
                </div>
                <div class="footer">
                    <p>&copy; 2025 Oppex AI. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>
        """, verificationLink, verificationLink);

        try {
            mailer.send(Mail.withHtml(email, subject, htmlBody));
            LOG.infof("Verification email sent to: %s", email);
            LOG.infof("Verification link: %s", verificationLink);
        } catch (Exception e) {
            LOG.errorf("Failed to send verification email to %s: %s", email, e.getMessage());
        }
    }
}
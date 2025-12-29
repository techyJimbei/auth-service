package org.oppexai.service;

import com.resend.*;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;
import jakarta.inject.Inject;

@ApplicationScoped
public class EmailService {
    private static final Logger LOG = Logger.getLogger(EmailService.class);

    @ConfigProperty(name = "resend.api.key")
    String apiKey;

    @ConfigProperty(name = "app.backend.url")
    String backendUrl;

    @Inject
    ManagedExecutor managedExecutor;

    public void sendVerificationEmail(String email, String token) {
        // Use ManagedExecutor to prevent blocking the main thread
        managedExecutor.runAsync(() -> {
            Resend resend = new Resend(apiKey);

            String link = backendUrl + "/api/auth/verify?token=" + token;
            String htmlBody = String.format("""
                <h1>Verify Your Email</h1>
                <p>Click the link below to verify your account:</p>
                <a href="%s">Verify Email Address</a>
                """, link);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("Oppex AI <onboarding@resend.dev>")
                    .to(email)
                    .subject("Verify Your Email - Oppex AI")
                    .html(htmlBody)
                    .build();

            try {
                CreateEmailResponse data = resend.emails().send(params);
                LOG.infof("Email sent successfully via Resend. ID: %s", data.getId());
            } catch (Exception e) {
                LOG.errorf("Resend API failed for %s: %s", email, e.getMessage());
            }
        });
    }
}
package org.oppexai.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer; // Use Reactive
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EmailService {
    private static final Logger LOG = Logger.getLogger(EmailService.class);

    @Inject
    ReactiveMailer reactiveMailer;

    @ConfigProperty(name = "app.backend.url")
    String backendUrl;

    public void sendVerificationEmail(String email, String token) {
        String link = backendUrl + "/api/auth/verify?token=" + token;
        String htmlBody = "<h1>Verify Email</h1><a href=\"" + link + "\">Click Here</a>";

        // Non-blocking send
        reactiveMailer.send(Mail.withHtml(email, "Verify Your Email", htmlBody))
                .subscribe().with(
                        success -> LOG.infof("Email sent to %s", email),
                        failure -> LOG.errorf("Email failed for %s: %s", email, failure.getMessage())
                );
    }
}
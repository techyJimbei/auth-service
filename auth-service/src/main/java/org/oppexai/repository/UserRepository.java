package org.oppexai.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.oppexai.model.User;

import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {


    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public Optional<User> findByVerificationToken(String token) {
        return find("verificationToken", token).firstResultOptional();
    }

    public boolean existsByEmail(String email) {
        return count("email", email) > 0;
    }

}
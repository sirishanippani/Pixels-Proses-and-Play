package com.project.blog.config;

import com.project.blog.model.Role;
import com.project.blog.model.User;
import com.project.blog.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a single admin user on first boot when deployed to a fresh database.
 *
 * <p>Runs only under the {@code postgres} profile so local H2 development and
 * tests are never affected. Credentials come from environment variables, which
 * means the admin password is never checked into git and can be rotated by
 * restarting the service with new env values.
 *
 * <p>Idempotent: if any user already has {@code ROLE_ADMIN}, this bootstrapper
 * does nothing on subsequent boots. Safe to leave enabled forever.
 */
@Component
@Profile("postgres")
public class AdminBootstrapper implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapper.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminEmail;
    private final String adminPassword;

    public AdminBootstrapper(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            org.springframework.core.env.Environment env
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = env.getProperty("ADMIN_USERNAME");
        this.adminEmail = env.getProperty("ADMIN_EMAIL");
        this.adminPassword = env.getProperty("ADMIN_PASSWORD");
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByRole(Role.ROLE_ADMIN)) {
            log.info("AdminBootstrapper: admin user already present; skipping seed");
            return;
        }

        if (isBlank(adminUsername) || isBlank(adminEmail) || isBlank(adminPassword)) {
            log.warn("AdminBootstrapper: no admin exists but ADMIN_USERNAME/EMAIL/PASSWORD are not all set; skipping. "
                    + "Set these env vars and restart to seed an admin.");
            return;
        }

        User admin = new User();
        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ROLE_ADMIN);
        userRepository.save(admin);

        log.info("AdminBootstrapper: seeded admin user '{}' ({}). Rotate the password by resetting "
                + "ADMIN_PASSWORD, deleting this user via /admin, and restarting.", adminUsername, adminEmail);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}

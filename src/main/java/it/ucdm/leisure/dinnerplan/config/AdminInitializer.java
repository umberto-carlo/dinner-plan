package it.ucdm.leisure.dinnerplan.config;

import it.ucdm.leisure.dinnerplan.features.user.Role;
import it.ucdm.leisure.dinnerplan.model.User;
import it.ucdm.leisure.dinnerplan.persistence.UserRepositoryPort;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(UserRepositoryPort userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner initAdmin() {
        return args -> {
            try {
                // Check if admin already exists
                if (userRepository.findByUsername("admin").isEmpty()) {
                    // Create admin user
                    User admin = new User();
                    admin.setUsername("admin");
                    admin.setPassword(passwordEncoder.encode("admin")); // You should change this later!
                    admin.setRole(Role.ADMIN);

                    userRepository.save(admin);
                    System.out.println("Admin user created successfully.");
                } else {
                    System.out.println("Admin user already exists.");
                }
            } catch (Exception e) {
                // In case database is not yet ready or other issues, just log
                System.err.println("Could not initialize admin user: " + e.getMessage());
            }
        };
    }
}

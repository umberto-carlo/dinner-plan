package it.ucdm.leisure.dinnerplan.features.user;

import it.ucdm.leisure.dinnerplan.features.geocode.Coordinates;
import it.ucdm.leisure.dinnerplan.features.geocode.GeocodingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GeocodingService geocodingService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, GeocodingService geocodingService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.geocodingService = geocodingService;
    }

    @Transactional
    public User registerUser(String username, String email, String password, Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        User user = User.builder()
                .username(username)
                .email((email == null || email.trim().isEmpty()) ? null : email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();

        return userRepository.save(Objects.requireNonNull(user));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void changePassword(String username, String newPassword) {
        User user = findByUsername(username);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(Objects.requireNonNull(user));
    }

    @Transactional
    public void updateEmail(String username, String newEmail) {
        User user = findByUsername(username);
        String processedNewEmail = (newEmail == null || newEmail.trim().isEmpty()) ? null : newEmail;

        if (!Objects.equals(user.getEmail(), processedNewEmail)) {
            if (processedNewEmail != null && userRepository.findByEmail(processedNewEmail).isPresent()) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(processedNewEmail);
            userRepository.save(Objects.requireNonNull(user));
        }
    }

    @Transactional
    public void updateAddress(String username, String newAddress) {
        User user = findByUsername(username);
        String processedAddress = (newAddress == null || newAddress.trim().isEmpty()) ? null : newAddress;
        
        // Update if address changed OR if address is present but coordinates are missing (force refresh)
        boolean addressChanged = !Objects.equals(user.getAddress(), processedAddress);
        boolean coordinatesMissing = processedAddress != null && (user.getLatitude() == null || user.getLongitude() == null);

        if (addressChanged || coordinatesMissing) {
            user.setAddress(processedAddress);

            // If the address has changed, the old coordinates are definitely invalid.
            // Reset them immediately.
            if (addressChanged) {
                user.setLatitude(null);
                user.setLongitude(null);
            }

            if (processedAddress != null) {
                try {
                    Coordinates coords = geocodingService.getCoordinates(processedAddress);
                    if (coords != null) {
                        user.setLatitude(coords.getLatitude());
                        user.setLongitude(coords.getLongitude());
                    } else {
                        logger.warn("Geocoding failed for address: {}", processedAddress);
                        // No need to reset here, they are already null if addressChanged was true.
                        // If address didn't change but coords were missing, they stay missing.
                    }
                } catch (Exception e) {
                    logger.error("Unexpected error during geocoding for user {}", username, e);
                    // Coordinates remain null (safe state)
                }
            } else {
                // Address removed, ensure coords are null
                user.setLatitude(null);
                user.setLongitude(null);
            }
            userRepository.save(Objects.requireNonNull(user));
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getAllUsersExceptAdmins() {
        return userRepository.findByRoleNot(it.ucdm.leisure.dinnerplan.features.user.Role.ADMIN);
    }

    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(Objects.requireNonNull(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(Objects.requireNonNull(user));
    }

    @Transactional
    public void promoteUserToOrganizer(Long userId) {
        User user = userRepository.findById(Objects.requireNonNull(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setRole(Role.ORGANIZER);
        userRepository.save(Objects.requireNonNull(user));
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(Objects.requireNonNull(userId));
    }

    @Transactional
    public void updateUserRole(Long userId, Role role) {
        User user = userRepository.findById(Objects.requireNonNull(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setRole(role);
        userRepository.save(Objects.requireNonNull(user));
    }
}

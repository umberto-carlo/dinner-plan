package it.ucdm.leisure.dinnerplan.features.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        user.setAddress(processedAddress);
        userRepository.save(Objects.requireNonNull(user));
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

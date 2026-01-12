package it.ucdm.leisure.dinnerplan.features.user;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    private final UserService userService;
    private final it.ucdm.leisure.dinnerplan.utils.UserAgentUtils userAgentUtils;
    private final org.springframework.context.MessageSource messageSource;

    public UserController(UserService userService, it.ucdm.leisure.dinnerplan.utils.UserAgentUtils userAgentUtils,
            org.springframework.context.MessageSource messageSource) {
        this.userService = userService;
        this.userAgentUtils = userAgentUtils;
        this.messageSource = messageSource;
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model,
            @org.springframework.web.bind.annotation.RequestHeader(value = "User-Agent", required = false) String userAgent) {
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("user", user);

        if (userAgentUtils.isMobile(userAgent)) {
            return "mobile/profile";
        }
        return "profile";
    }

    @PostMapping("/profile/change-password")
    @SuppressWarnings("null")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String newPassword,
            Model model,
            @org.springframework.web.bind.annotation.RequestHeader(value = "User-Agent", required = false) String userAgent,
            java.util.Locale locale) {
        try {
            userService.changePassword(userDetails.getUsername(), newPassword);
            model.addAttribute("passwordSuccess", messageSource.getMessage("profile.password_success", null, locale));
        } catch (Exception e) {
            model.addAttribute("passwordError", messageSource.getMessage("profile.password_error", null, locale));
        }
        // Reload user to keep consistency
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("user", user);

        if (userAgentUtils.isMobile(userAgent)) {
            return "mobile/profile";
        }
        return "profile";
    }

    @PostMapping("/profile/update-email")
    public String updateEmail(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String newEmail,
            Model model,
            @org.springframework.web.bind.annotation.RequestHeader(value = "User-Agent", required = false) String userAgent,
            java.util.Locale locale) {
        try {
            userService.updateEmail(userDetails.getUsername(), newEmail);
            model.addAttribute("emailSuccess", messageSource.getMessage("profile.email_success", null, locale));
        } catch (IllegalArgumentException e) {
            if ("Email already exists".equals(e.getMessage())) {
                model.addAttribute("emailError", messageSource.getMessage("profile.email_exists", null, locale));
            } else {
                model.addAttribute("emailError", messageSource.getMessage("profile.email_error", null, locale));
            }
        } catch (Exception e) {
            model.addAttribute("emailError", messageSource.getMessage("profile.email_error", null, locale));
        }
        // Reload user to keep consistency
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("user", user);

        if (userAgentUtils.isMobile(userAgent)) {
            return "mobile/profile";
        }
        return "profile";
    }

    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    @GetMapping("/admin/users")
    public String listUsers(@AuthenticationPrincipal UserDetails userDetails, Model model,
            @org.springframework.web.bind.annotation.RequestHeader(value = "User-Agent", required = false) String userAgent) {
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            model.addAttribute("users", userService.getAllUsers());
        } else {
            model.addAttribute("users", userService.getAllUsersExceptAdmins());
        }

        if (userAgentUtils.isMobile(userAgent)) {
            return "mobile/user_list";
        }
        return "user_list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/users/create")
    public String createUser(@RequestParam String username, @RequestParam String email, @RequestParam String password,
            @RequestParam String role) {
        userService.registerUser(username, email, password,
                it.ucdm.leisure.dinnerplan.features.user.Role.valueOf(role));
        return "redirect:/admin/users";
    }

    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    @PostMapping("/admin/users/{id}/reset-password")
    public String resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return "redirect:/admin/users?success";
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/admin/users/{id}/promote")
    public String promoteUser(@PathVariable Long id) {
        userService.promoteUserToOrganizer(id);
        return "redirect:/admin/users";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/users/{id}/role")
    public String changeUserRole(@PathVariable Long id, @RequestParam String role) {
        userService.updateUserRole(id, it.ucdm.leisure.dinnerplan.features.user.Role.valueOf(role));
        return "redirect:/admin/users";
    }
}

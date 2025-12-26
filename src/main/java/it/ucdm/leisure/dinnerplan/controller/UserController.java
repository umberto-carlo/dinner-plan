package it.ucdm.leisure.dinnerplan.controller;

import it.ucdm.leisure.dinnerplan.model.User;
import it.ucdm.leisure.dinnerplan.service.UserService;
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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String newPassword,
            Model model) {
        try {
            userService.changePassword(userDetails.getUsername(), newPassword);
            model.addAttribute("success", "Password aggiornata con successo");
        } catch (Exception e) {
            model.addAttribute("error", "Errore durante l'aggiornamento della password");
        }
        // Reload user to keep consistency
        User user = userService.findByUsername(userDetails.getUsername());
        model.addAttribute("user", user);
        return "profile";
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping("/admin/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "user_list";
    }

    @PreAuthorize("hasRole('ORGANIZER')")
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
}

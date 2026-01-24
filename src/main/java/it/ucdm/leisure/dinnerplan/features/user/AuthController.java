package it.ucdm.leisure.dinnerplan.features.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username, @RequestParam(required = false) String email, @RequestParam String password,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String dietaryPreference,
            Model model) {
        try {
            DietaryPreference preference = DietaryPreference.OMNIVORE;
            if (dietaryPreference != null && !dietaryPreference.isEmpty()) {
                preference = DietaryPreference.valueOf(dietaryPreference);
            }
            User user = userService.registerUser(username, email, password,
                    it.ucdm.leisure.dinnerplan.features.user.Role.PARTICIPANT, preference);
            
            if (address != null && !address.trim().isEmpty()) {
                userService.updateAddress(user.getUsername(), address);
            }

            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}

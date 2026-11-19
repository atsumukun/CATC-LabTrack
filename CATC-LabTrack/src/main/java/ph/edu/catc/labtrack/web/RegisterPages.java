package ph.edu.catc.labtrack.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ph.edu.catc.labtrack.equipment.service.EquipmentService;

@Controller
public class RegisterPages {
    private final EquipmentService service;
    public RegisterPages(EquipmentService service) { this.service = service; }

    @GetMapping("/login")
    String login(Authentication account) {
        return account == null ? "login" : "redirect:/";
    }

    @GetMapping("/")
    String home(Authentication account) {
        boolean admin = account.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return admin ? "redirect:/admin" : "redirect:/user";
    }

    @GetMapping("/user")
    String user(@RequestParam(defaultValue = "") String location, Model model) {
        return register(location, model, "user");
    }

    @GetMapping("/admin")
    String admin(@RequestParam(defaultValue = "") String location, Model model) {
        return register(location, model, "admin");
    }

    private String register(String location, Model model, String role) {
        model.addAttribute("items", service.list(location));
        model.addAttribute("location", location);
        return role + "/register";
    }
}

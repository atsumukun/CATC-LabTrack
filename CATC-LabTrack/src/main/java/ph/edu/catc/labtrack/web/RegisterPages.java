package ph.edu.catc.labtrack.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ph.edu.catc.labtrack.equipment.model.Status;
import ph.edu.catc.labtrack.equipment.service.EquipmentService;
import ph.edu.catc.labtrack.reservation.service.ReservationService;

@Controller
public class RegisterPages {
    private final EquipmentService service;
    private final ReservationService reservations;

    public RegisterPages(EquipmentService service, ReservationService reservations) {
        this.service = service;
        this.reservations = reservations;
    }

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
    String user(@RequestParam(defaultValue = "") String location,
                @RequestParam(required = false) Status status,
                @RequestParam(defaultValue = "") String q,
                Authentication account, Model model) {
        var all = service.list(location);
        var items = status == null ? all : all.stream().filter(i -> i.status() == status).toList();

        model.addAttribute("items", items);
        model.addAttribute("location", location);
        model.addAttribute("status", status == null ? null : status.name());
        model.addAttribute("q", q);
        model.addAttribute("statuses", Status.values());
        model.addAttribute("username", account.getName());
        model.addAttribute("maxQty", ReservationService.MAX_PER_REQUEST);
        model.addAttribute("totalCount", all.size());
        model.addAttribute("availableCount", all.stream().filter(i -> i.availableUnits() > 0).count());
        model.addAttribute("reservations", reservations.mine(account.getName()));
        return "user/register";
    }

    @GetMapping("/user/manage")
    String manage(@RequestParam(defaultValue = "") String location, Model model) {
        model.addAttribute("items", service.list(location));
        model.addAttribute("location", location);
        return "user/manage";
    }

    @GetMapping("/admin")
    String admin(@RequestParam(defaultValue = "") String location, Model model) {
        model.addAttribute("items", service.list(location));
        model.addAttribute("location", location);
        model.addAttribute("reservations", reservations.register());
        return "admin/register";
    }
}
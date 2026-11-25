package ph.edu.catc.labtrack.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ph.edu.catc.labtrack.equipment.model.Status;
import ph.edu.catc.labtrack.equipment.service.EquipmentService;
import ph.edu.catc.labtrack.profile.ProfileService;
import ph.edu.catc.labtrack.reservation.model.ReservationStatus;
import ph.edu.catc.labtrack.reservation.service.ReservationService;

@Controller
public class RegisterPages {
    private final EquipmentService service;
    private final ReservationService reservations;
    private final ProfileService profiles;

    public RegisterPages(EquipmentService service, ReservationService reservations, ProfileService profiles) {
        this.service = service;
        this.reservations = reservations;
        this.profiles = profiles;
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

    /** Values every student page needs. */
    private void common(Authentication account, Model model) {
        model.addAttribute("username", account.getName());
        model.addAttribute("profile", profiles.find(account.getName()).orElse(null));
        model.addAttribute("maxQty", ReservationService.MAX_PER_REQUEST);
        model.addAttribute("returnDays", ReservationService.RETURN_DAYS);
        model.addAttribute("lateFine", ReservationService.LATE_FINE_PER_DAY);
    }

    @GetMapping("/user")
    String user(@RequestParam(defaultValue = "") String location,
                @RequestParam(required = false) Status status,
                @RequestParam(defaultValue = "") String q, Authentication account, Model model) {
        var all = service.list(location);
        var items = status == null ? all : all.stream().filter(i -> i.status() == status).toList();

        common(account, model);
        model.addAttribute("items", items);
        model.addAttribute("location", location);
        model.addAttribute("status", status == null ? null : status.name());
        model.addAttribute("q", q);
        model.addAttribute("statuses", Status.values());
        model.addAttribute("totalCount", all.size());
        model.addAttribute("availableCount", all.stream().filter(i -> i.availableUnits() > 0).count());
        model.addAttribute("reservationCount", reservations.mine(account.getName()).size());
        return "user/register";
    }

    @GetMapping("/user/reservations")
    String myReservations(Authentication account, Model model) {
        var mine = reservations.mine(account.getName());

        common(account, model);
        model.addAttribute("reservations", mine);
        model.addAttribute("pendingCount", mine.stream().filter(r -> r.status() == ReservationStatus.PENDING).count());
        model.addAttribute("borrowedCount", mine.stream().filter(r -> r.status() == ReservationStatus.BORROWED).count());
        model.addAttribute("returnedCount", mine.stream().filter(r -> r.status() == ReservationStatus.RETURNED).count());
        return "user/reservations";
    }

    @GetMapping("/user/profile")
    String myProfile(Authentication account, Model model) {
        common(account, model);
        return "user/profile";
    }

    @GetMapping("/user/manage")
    String manage(@RequestParam(defaultValue = "") String location, Authentication account, Model model) {
        common(account, model);
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
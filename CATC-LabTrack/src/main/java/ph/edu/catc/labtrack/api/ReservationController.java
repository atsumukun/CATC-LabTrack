package ph.edu.catc.labtrack.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ph.edu.catc.labtrack.reservation.dto.ReservationRequest;
import ph.edu.catc.labtrack.reservation.dto.ReservationResponse;
import ph.edu.catc.labtrack.reservation.service.ReservationService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService service;

    public ReservationController(ReservationService service) { this.service = service; }

    @PostMapping
    ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationRequest input, Authentication account) {
        var saved = service.reserve(input, account.getName());
        return ResponseEntity.created(URI.create("/api/reservations/" + saved.id())).body(saved);
    }

    @GetMapping("/mine")
    List<ReservationResponse> mine(Authentication account) {
        return service.mine(account.getName());
    }
}
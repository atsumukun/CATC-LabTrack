package ph.edu.catc.labtrack.api;

import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ph.edu.catc.labtrack.equipment.dto.*;
import ph.edu.catc.labtrack.equipment.service.EquipmentService;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {
    private final EquipmentService service;

    public EquipmentController(EquipmentService service) { this.service = service; }

    @GetMapping
    List<EquipmentResponse> list(@RequestParam(defaultValue = "") String location) {
        return service.list(location);
    }

    @GetMapping("/{id}")
    ResponseEntity<EquipmentResponse> find(@PathVariable long id) {
        return ResponseEntity.of(service.find(id));
    }

    @PostMapping
    ResponseEntity<EquipmentResponse> create(@Valid @RequestBody EquipmentRequest input) {
        var saved = service.create(input);
        return ResponseEntity.created(URI.create("/api/equipment/" + saved.id())).body(saved);
    }
}

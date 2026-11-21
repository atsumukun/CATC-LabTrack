package ph.edu.catc.labtrack.api;

import ph.edu.catc.labtrack.equipment.model.Status;
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
    List<EquipmentResponse> list(@RequestParam(defaultValue = "") String location,
                                 @RequestParam(required = false) Status status) {
        return service.list(location, status);
    }

    @GetMapping("/{id}")
    ResponseEntity<EquipmentResponse> find(@PathVariable long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @PutMapping("/{id}")
    EquipmentResponse update(@PathVariable long id, @Valid @RequestBody EquipmentRequest input) {
        return service.update(id, input);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    ResponseEntity<EquipmentResponse> create(@Valid @RequestBody EquipmentRequest input) {
        var saved = service.create(input);
        return ResponseEntity.created(URI.create("/api/equipment/" + saved.id())).body(saved);
    }
}

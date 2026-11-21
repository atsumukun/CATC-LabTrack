package ph.edu.catc.labtrack.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ph.edu.catc.labtrack.equipment.dto.EquipmentRequest;
import ph.edu.catc.labtrack.equipment.dto.EquipmentResponse;
import ph.edu.catc.labtrack.equipment.model.Status;
import ph.edu.catc.labtrack.equipment.service.EquipmentService;

import java.net.URI;
import java.util.List;

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
        return ResponseEntity.of(service.find(id));
    }

    @PostMapping
    ResponseEntity<EquipmentResponse> create(@Valid @RequestBody EquipmentRequest input) {
        var saved = service.create(input);
        return ResponseEntity.created(URI.create("/api/equipment/" + saved.id())).body(saved);
    }
}
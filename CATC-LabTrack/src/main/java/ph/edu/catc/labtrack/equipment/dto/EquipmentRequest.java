package ph.edu.catc.labtrack.equipment.dto;

import jakarta.validation.constraints.*;
import ph.edu.catc.labtrack.equipment.model.Status;

public record EquipmentRequest(
    @NotBlank @Pattern(regexp = "[A-Za-z0-9-]{1,32}", message = "Use 1–32 letters, digits or hyphens") String assetCode,
    @NotBlank @Size(max = 80) String name,
    @NotBlank @Size(max = 80) String location,
    @NotNull Status status
) {}

package ph.edu.catc.labtrack.equipment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ph.edu.catc.labtrack.equipment.model.Status;

public record EquipmentRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9-]{1,32}", message = "Use 1-32 letters, digits or hyphens") String assetCode,
        @NotBlank @Size(max = 80) String name,
        @Size(max = 255) String description,
        @Size(max = 255) String imageUrl,
        @NotBlank @Size(max = 80) String location,
        @NotNull Status status,
        @Min(1) @Max(99) Integer totalUnits
) {}
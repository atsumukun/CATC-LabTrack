package ph.edu.catc.labtrack.equipment.dto;

import ph.edu.catc.labtrack.equipment.model.Equipment;
import ph.edu.catc.labtrack.equipment.model.Status;

public record EquipmentResponse(Long id, String assetCode, String name, String description, String imageUrl,
                                String location, Status status, int totalUnits, int availableUnits) {
    public static EquipmentResponse from(Equipment item) {
        return new EquipmentResponse(item.getId(), item.getAssetCode(), item.getName(), item.getDescription(),
                item.getImageUrl(), item.getLocation(), item.getStatus(), item.getTotalUnits(),
                item.getAvailableUnits());
    }
}
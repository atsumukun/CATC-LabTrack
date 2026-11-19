package ph.edu.catc.labtrack.equipment.dto;

import ph.edu.catc.labtrack.equipment.model.*;

public record EquipmentResponse(Long id, String assetCode, String name, String location, Status status) {
    public static EquipmentResponse from(Equipment item) {
        return new EquipmentResponse(item.getId(), item.getAssetCode(), item.getName(), item.getLocation(), item.getStatus());
    }
}

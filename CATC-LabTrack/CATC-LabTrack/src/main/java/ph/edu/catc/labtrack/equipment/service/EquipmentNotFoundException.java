package ph.edu.catc.labtrack.equipment.service;

public class EquipmentNotFoundException extends RuntimeException {
    public EquipmentNotFoundException(long id) {
        super("Equipment " + id + " was not found.");
    }
}

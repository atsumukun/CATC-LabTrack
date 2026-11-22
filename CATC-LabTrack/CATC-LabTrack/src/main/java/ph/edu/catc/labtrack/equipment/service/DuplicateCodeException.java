package ph.edu.catc.labtrack.equipment.service;

public class DuplicateCodeException extends RuntimeException {
    public DuplicateCodeException(String code) { super("Asset code " + code + " is already registered."); }
}

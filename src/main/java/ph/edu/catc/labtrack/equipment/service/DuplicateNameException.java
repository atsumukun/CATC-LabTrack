package ph.edu.catc.labtrack.equipment.service;

public class DuplicateNameException extends RuntimeException {
    public DuplicateNameException(String name) {
        super("Equipment \"" + name + "\" is already registered.");
    }
}
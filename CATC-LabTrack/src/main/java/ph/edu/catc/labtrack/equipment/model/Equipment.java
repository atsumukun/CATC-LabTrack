package ph.edu.catc.labtrack.equipment.model;

import jakarta.persistence.*;

@Entity
public class Equipment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 32)
    private String assetCode;
    @Column(nullable = false, length = 80)
    private String name;
    @Column(nullable = false, length = 80)
    private String location;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Status status;

    protected Equipment() {}

    public Equipment(String assetCode, String name, String location, Status status) {
        this.assetCode = assetCode;
        this.name = name;
        this.location = location;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getAssetCode() { return assetCode; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public Status getStatus() { return status; }
}

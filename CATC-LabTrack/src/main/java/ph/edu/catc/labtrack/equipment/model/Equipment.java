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
    @Column(length = 255)
    private String description;
    @Column(length = 255)
    private String imageUrl;
    @Column(nullable = false, length = 80)
    private String location;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Status status;
    @Column(nullable = false)
    private int totalUnits;
    @Column(nullable = false)
    private int availableUnits;

    protected Equipment() {}

    public Equipment(String assetCode, String name, String description, String imageUrl,
                     String location, Status status, int totalUnits) {
        this.assetCode = assetCode;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.location = location;
        this.status = status;
        this.totalUnits = totalUnits;
        // Only an AVAILABLE item starts with free units.
        this.availableUnits = status == Status.AVAILABLE ? totalUnits : 0;
    }

    /** Holds units for a pending reservation. When none are left the item shows as RESERVED. */
    public void reserveUnits(int quantity) {
        if (quantity < 1 || quantity > availableUnits) {
            throw new IllegalArgumentException("Not enough free units");
        }
        availableUnits -= quantity;
        if (availableUnits == 0) {
            status = Status.RESERVED;
        }
    }

    public Long getId() { return id; }
    public String getAssetCode() { return assetCode; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getLocation() { return location; }
    public Status getStatus() { return status; }
    public int getTotalUnits() { return totalUnits; }
    public int getAvailableUnits() { return availableUnits; }
}
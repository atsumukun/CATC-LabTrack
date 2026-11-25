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

    @Column(nullable = false)
    private int heldUnits;

    private int borrowedUnits;

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
        heldUnits += quantity;
        if (availableUnits == 0) {
            status = Status.RESERVED;
        }
    }

    public void pickUpUnits(int quantity) {
        borrowedUnits += quantity;
        status = stockStatus(totalUnits);
    }

    public void releaseUnits(int quantity, boolean wasBorrowed) {
        heldUnits -= quantity;
        if (wasBorrowed) borrowedUnits -= quantity;
        availableUnits += quantity;
        status = stockStatus(totalUnits);
    }

    private Status stockStatus(int capacity) {
        if (capacity > heldUnits) return Status.AVAILABLE;
        return borrowedUnits == capacity ? Status.BORROWED : Status.RESERVED;
    }

    public void update(String code, String name, String location, Status status, Integer total,
                       String description, String imageUrl) {
        int capacity = total == null ? totalUnits : total;
        int held = heldUnits;
        if (held > capacity || (held > 0 && status != stockStatus(capacity))) {
            throw new ph.edu.catc.labtrack.reservation.service.ReservationException(
                "This change conflicts with held units.", org.springframework.http.HttpStatus.CONFLICT);
        }
        assetCode = code;
        this.name = name;
        this.location = location;
        this.status = status;
        totalUnits = capacity;
        if (held > 0) {
            availableUnits = capacity - held;

        } else {
            availableUnits = status == Status.AVAILABLE ? capacity : 0;
        }
        if (description != null) this.description = description.strip();
        if (imageUrl != null) this.imageUrl = imageUrl.strip();
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
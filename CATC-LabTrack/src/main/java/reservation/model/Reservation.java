package ph.edu.catc.labtrack.reservation.model;

import jakarta.persistence.*;
import ph.edu.catc.labtrack.equipment.model.Equipment;

import java.time.Instant;
import java.time.LocalDate;

@Entity
public class Reservation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Equipment equipment;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false, length = 80)
    private String borrowerName;
    @Column(nullable = false, length = 30)
    private String yearLevel;
    @Column(nullable = false, length = 80)
    private String course;
    @Column(nullable = false, length = 80)
    private String subject;
    @Column(nullable = false)
    private LocalDate useDate;
    @Column(nullable = false, length = 64)
    private String requestedBy;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ReservationStatus status;
    @Column(nullable = false)
    private Instant createdAt;

    protected Reservation() {}

    public Reservation(Equipment equipment, int quantity, String borrowerName, String yearLevel,
                       String course, String subject, LocalDate useDate, String requestedBy) {
        this.equipment = equipment;
        this.quantity = quantity;
        this.borrowerName = borrowerName;
        this.yearLevel = yearLevel;
        this.course = course;
        this.subject = subject;
        this.useDate = useDate;
        this.requestedBy = requestedBy;
        this.status = ReservationStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Equipment getEquipment() { return equipment; }
    public int getQuantity() { return quantity; }
    public String getBorrowerName() { return borrowerName; }
    public String getYearLevel() { return yearLevel; }
    public String getCourse() { return course; }
    public String getSubject() { return subject; }
    public LocalDate getUseDate() { return useDate; }
    public String getRequestedBy() { return requestedBy; }
    public ReservationStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
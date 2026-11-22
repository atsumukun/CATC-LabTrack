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
    // Copied from the student's profile at the time of booking, so old records never change.
    @Column(nullable = false, length = 80)
    private String borrowerName;
    @Column(nullable = false, length = 32)
    private String studentId;
    @Column(nullable = false, length = 30)
    private String yearLevel;
    @Column(nullable = false, length = 120)
    private String program;
    @Column(nullable = false, length = 80)
    private String instructorName;
    @Column(nullable = false, length = 80)
    private String subject;
    @Column(nullable = false)
    private LocalDate useDate;
    @Column(nullable = false)
    private LocalDate returnDate;
    @Column(nullable = false, length = 64)
    private String requestedBy;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ReservationStatus status;
    @Column(nullable = false)
    private Instant createdAt;

    protected Reservation() {}

    public Reservation(Equipment equipment, int quantity, String borrowerName, String studentId,
                       String yearLevel, String program, String instructorName, String subject,
                       LocalDate useDate, LocalDate returnDate, String requestedBy) {
        this.equipment = equipment;
        this.quantity = quantity;
        this.borrowerName = borrowerName;
        this.studentId = studentId;
        this.yearLevel = yearLevel;
        this.program = program;
        this.instructorName = instructorName;
        this.subject = subject;
        this.useDate = useDate;
        this.returnDate = returnDate;
        this.requestedBy = requestedBy;
        this.status = ReservationStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public void transitionTo(ReservationStatus next) {
        this.status = next;
    }

    public Long getId() { return id; }
    public Equipment getEquipment() { return equipment; }
    public int getQuantity() { return quantity; }
    public String getBorrowerName() { return borrowerName; }
    public String getStudentId() { return studentId; }
    public String getYearLevel() { return yearLevel; }
    public String getProgram() { return program; }
    public String getInstructorName() { return instructorName; }
    public String getSubject() { return subject; }
    public LocalDate getUseDate() { return useDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public String getRequestedBy() { return requestedBy; }
    public ReservationStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
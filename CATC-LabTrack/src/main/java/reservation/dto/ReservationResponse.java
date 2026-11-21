package ph.edu.catc.labtrack.reservation.dto;

import ph.edu.catc.labtrack.reservation.model.Reservation;
import ph.edu.catc.labtrack.reservation.model.ReservationStatus;

import java.time.Instant;
import java.time.LocalDate;

public record ReservationResponse(Long id, String reference, Long equipmentId, String assetCode,
                                  String equipmentName, int quantity, String borrowerName, String yearLevel,
                                  String course, String subject, LocalDate useDate, ReservationStatus status,
                                  Instant createdAt) {
    /** Call inside a transaction: it reads the lazily loaded equipment. */
    public static ReservationResponse from(Reservation r) {
        var item = r.getEquipment();
        return new ReservationResponse(r.getId(), "RSV-%05d".formatted(r.getId()), item.getId(),
                item.getAssetCode(), item.getName(), r.getQuantity(), r.getBorrowerName(), r.getYearLevel(),
                r.getCourse(), r.getSubject(), r.getUseDate(), r.getStatus(), r.getCreatedAt());
    }
}
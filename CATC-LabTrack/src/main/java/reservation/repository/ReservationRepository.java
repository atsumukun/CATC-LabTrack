package ph.edu.catc.labtrack.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ph.edu.catc.labtrack.reservation.model.Reservation;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByRequestedByOrderByCreatedAtDesc(String requestedBy);
}
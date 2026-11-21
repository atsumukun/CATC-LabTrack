package ph.edu.catc.labtrack.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ph.edu.catc.labtrack.reservation.model.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByIdAndRequestedBy(Long id, String requestedBy);

    List<Reservation> findByRequestedByOrderByCreatedAtDesc(String requestedBy);
}
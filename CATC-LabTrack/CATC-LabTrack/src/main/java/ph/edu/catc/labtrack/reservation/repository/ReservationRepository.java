package ph.edu.catc.labtrack.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ph.edu.catc.labtrack.reservation.model.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select r from Reservation r where r.id = :id")
    Optional<Reservation> findForUpdate(@org.springframework.data.repository.query.Param("id") long id);

    List<Reservation> findAllByOrderByCreatedAtDesc();

    Optional<Reservation> findByIdAndRequestedBy(Long id, String requestedBy);

    List<Reservation> findByRequestedByOrderByCreatedAtDesc(String requestedBy);
}
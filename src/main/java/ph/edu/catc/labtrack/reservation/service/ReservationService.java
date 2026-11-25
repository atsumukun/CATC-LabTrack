package ph.edu.catc.labtrack.reservation.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ph.edu.catc.labtrack.equipment.repository.EquipmentRepository;
import ph.edu.catc.labtrack.profile.ProfileService;
import ph.edu.catc.labtrack.reservation.dto.ReservationRequest;
import ph.edu.catc.labtrack.reservation.dto.ReservationResponse;
import ph.edu.catc.labtrack.reservation.model.Reservation;
import ph.edu.catc.labtrack.reservation.repository.ReservationRepository;

import java.util.List;
import ph.edu.catc.labtrack.reservation.model.ReservationStatus;

@Service
public class ReservationService {
    /** Most units one person can reserve in a single request. */
    public static final int MAX_PER_REQUEST = 3;
    /** Equipment must be returned this many days after the borrow date. */
    public static final int RETURN_DAYS = 3;
    /** Fine in pesos for each day an item is returned late. */
    public static final int LATE_FINE_PER_DAY = 10;

    private final ReservationRepository reservations;
    private final EquipmentRepository equipment;
    private final ProfileService profiles;

    public ReservationService(ReservationRepository reservations, EquipmentRepository equipment,
                              ProfileService profiles) {
        this.reservations = reservations;
        this.equipment = equipment;
        this.profiles = profiles;
    }

    @Transactional
    public ReservationResponse reserve(ReservationRequest input, String username) {
        var profile = profiles.find(username).orElseThrow(() -> new ReservationException(
                "Your student profile isn't set up yet. Please see the laboratory custodian.", HttpStatus.CONFLICT));

        var item = equipment.findForUpdate(input.equipmentId())
                .orElseThrow(() -> new ReservationException("That equipment no longer exists.", HttpStatus.NOT_FOUND));

        int free = item.getAvailableUnits();
        if (free == 0) {
            throw new ReservationException("No units of " + item.getName() + " are free right now.", HttpStatus.CONFLICT);
        }
        if (input.quantity() > free) {
            throw new ReservationException("Only " + free + (free == 1 ? " unit is" : " units are")
                    + " free right now. Lower the quantity and try again.", HttpStatus.CONFLICT);
        }

        item.reserveUnits(input.quantity());
        var saved = reservations.save(new Reservation(item, input.quantity(), profile.fullName(),
                profile.studentId(), profile.yearLevel(), profile.program(), input.instructorName() == null ? "Unspecified" : input.instructorName().strip(),
                input.subject().strip(), input.useDate(), input.useDate().plusDays(RETURN_DAYS), username));
        return ReservationResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> register() {
        return reservations.findAllByOrderByCreatedAtDesc().stream().map(ReservationResponse::from).toList();
    }

    @Transactional
    public ReservationResponse transition(long id, ReservationStatus next) {
        var booking = reservations.findForUpdate(id).orElseThrow(() ->
                new ReservationException("Reservation not found.", HttpStatus.NOT_FOUND));
        var current = booking.getStatus();
        boolean pickup = current == ReservationStatus.PENDING && next == ReservationStatus.BORROWED;
        boolean cancel = current == ReservationStatus.PENDING && next == ReservationStatus.CANCELLED;
        boolean returned = current == ReservationStatus.BORROWED && next == ReservationStatus.RETURNED;
        if (!pickup && !cancel && !returned) {
            throw new ReservationException("This reservation is already " + current + ".", HttpStatus.CONFLICT);
        }
        var item = equipment.findForUpdate(booking.getEquipment().getId()).orElseThrow();
        if (pickup) item.pickUpUnits(booking.getQuantity());
        else item.releaseUnits(booking.getQuantity(), returned);
        booking.transitionTo(next);
        return ReservationResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public ReservationResponse get(long id, String username) {
        return reservations.findByIdAndRequestedBy(id, username).map(ReservationResponse::from)
                .orElseThrow(() -> new ReservationException("Reservation not found.", HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> mine(String username) {
        return reservations.findByRequestedByOrderByCreatedAtDesc(username).stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
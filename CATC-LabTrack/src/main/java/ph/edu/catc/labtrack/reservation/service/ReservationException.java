package ph.edu.catc.labtrack.reservation.service;

import org.springframework.http.HttpStatus;

public class ReservationException extends RuntimeException {
    private final HttpStatus status;

    public ReservationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus status() { return status; }
}
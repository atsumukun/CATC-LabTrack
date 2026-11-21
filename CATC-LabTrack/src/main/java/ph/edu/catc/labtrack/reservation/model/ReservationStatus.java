package ph.edu.catc.labtrack.reservation.model;

/** PENDING = waiting for the custodian, BORROWED = confirmed and released, RETURNED = back in the lab. */
public enum ReservationStatus { PENDING, BORROWED, RETURNED, CANCELLED }
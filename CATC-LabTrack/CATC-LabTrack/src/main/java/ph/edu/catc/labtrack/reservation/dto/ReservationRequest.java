package ph.edu.catc.labtrack.reservation.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ph.edu.catc.labtrack.reservation.service.ReservationService;

import java.time.LocalDate;

/** The student's own details come from their profile on the server, so they are not sent here. */
public record ReservationRequest(
        @NotNull(message = "Choose the equipment to reserve") Long equipmentId,
        @Min(value = 1, message = "Choose at least 1 unit")
        @Max(value = ReservationService.MAX_PER_REQUEST, message = "You can reserve up to 3 units per request")
        int quantity,
        @jakarta.validation.constraints.Pattern(regexp = "(?s).*\\S.*", message = "Enter an instructor name")
        @Size(max = 80, message = "Instructor name is too long (80 characters max)") String instructorName,
        @NotBlank(message = "Please enter the subject you will use it for")
        @Size(max = 80, message = "Subject is too long (80 characters max)") String subject,
        @NotNull(message = "Please pick the date you will borrow it")
        @FutureOrPresent(message = "Borrow date can't be in the past") LocalDate useDate
) {}
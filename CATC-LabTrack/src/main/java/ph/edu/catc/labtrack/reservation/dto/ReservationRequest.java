package ph.edu.catc.labtrack.reservation.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ph.edu.catc.labtrack.reservation.service.ReservationService;

import java.time.LocalDate;

public record ReservationRequest(
        @NotNull(message = "Choose the equipment to reserve")
        Long equipmentId,

        @Min(value = 1, message = "Choose at least {value} unit")
        @Max(value = ReservationService.MAX_PER_REQUEST, message = "You can reserve up to {value} units per request")
        int quantity,

        @NotBlank(message = "Please enter your full name")
        @Size(max = 80, message = "Name is too long ({max} characters max)")
        String borrowerName,

        @NotBlank(message = "Please choose your year level")
        @Size(max = 30, message = "Year level is too long")
        String yearLevel,

        @NotBlank(message = "Please enter your course")
        @Size(max = 80, message = "Course is too long ({max} characters max)")
        String course,

        @NotBlank(message = "Please enter the subject you will use it for")
        @Size(max = 80, message = "Subject is too long ({max} characters max)")
        String subject,

        @NotNull(message = "Please pick the date of use")
        @FutureOrPresent(message = "Date of use can't be in the past")
        LocalDate useDate
) {}
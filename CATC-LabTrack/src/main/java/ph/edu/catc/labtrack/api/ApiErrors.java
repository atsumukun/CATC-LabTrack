package ph.edu.catc.labtrack.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ph.edu.catc.labtrack.equipment.service.DuplicateCodeException;
import ph.edu.catc.labtrack.reservation.service.ReservationException;

import java.util.Map;
import java.util.Objects;

@RestControllerAdvice(basePackageClasses = EquipmentController.class)
public class ApiErrors {
    @ExceptionHandler(DuplicateCodeException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    Map<String, String> duplicate(DuplicateCodeException ex) { return Map.of("message", ex.getMessage()); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, String> invalid(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors();
        if (errors.isEmpty()) {
            return Map.of("message", "Some values are not valid. Please check the form.");
        }
        var error = errors.getFirst();
        return Map.of("field", error.getField(),
                "message", Objects.requireNonNullElse(error.getDefaultMessage(), "Invalid value"));
    }

    @ExceptionHandler(ReservationException.class)
    ResponseEntity<Map<String, String>> reservation(ReservationException ex) {
        return ResponseEntity.status(ex.status()).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, String> unreadable(HttpMessageNotReadableException ex) {
        return Map.of("message", "The request could not be read. Please check the values and try again.");
    }
}
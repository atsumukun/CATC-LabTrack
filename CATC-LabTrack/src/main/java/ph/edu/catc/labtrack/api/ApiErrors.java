package ph.edu.catc.labtrack.api;

import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ph.edu.catc.labtrack.equipment.service.DuplicateCodeException;

@RestControllerAdvice(basePackageClasses = EquipmentController.class)
public class ApiErrors {
    @ExceptionHandler(DuplicateCodeException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    Map<String,String> duplicate(DuplicateCodeException ex) { return Map.of("message", ex.getMessage()); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String,String> invalid(MethodArgumentNotValidException ex) {
        var error = ex.getBindingResult().getFieldErrors().getFirst();
        return Map.of("field", error.getField(), "message", error.getDefaultMessage());
    }
}

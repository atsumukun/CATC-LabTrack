package ph.edu.catc.labtrack.api;

import java.util.Map;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
public class CsrfController {
    @GetMapping("/api/csrf")
    Map<String,String> token(CsrfToken token) {
        return Map.of("header", token.getHeaderName(), "token", token.getToken());
    }
}

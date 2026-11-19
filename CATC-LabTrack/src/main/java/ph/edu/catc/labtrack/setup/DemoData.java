package ph.edu.catc.labtrack.setup;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ph.edu.catc.labtrack.equipment.dto.EquipmentRequest;
import ph.edu.catc.labtrack.equipment.model.Status;
import ph.edu.catc.labtrack.equipment.service.EquipmentService;

@Configuration
public class DemoData {
    @Bean
    CommandLineRunner seed(JdbcTemplate jdbc, BCryptPasswordEncoder encoder, EquipmentService equipment) {
        return args -> {
            String sql = "insert into app_user(username,password_hash,role) values (?,?,?)";
            jdbc.update(sql, "user", encoder.encode("user-demo"), "USER");
            jdbc.update(sql, "admin", encoder.encode("admin-demo"), "ADMIN");
            equipment.create(new EquipmentRequest("EE-001", "Digital multimeter", "Electrical Engineering Lab", Status.AVAILABLE));
            equipment.create(new EquipmentRequest("CPE-001", "Breadboard", "Computer Engineering Lab", Status.RESERVED));
        };
    }
}

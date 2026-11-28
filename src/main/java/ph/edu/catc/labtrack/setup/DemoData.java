package ph.edu.catc.labtrack.setup;



import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ph.edu.catc.labtrack.equipment.dto.EquipmentRequest;
import ph.edu.catc.labtrack.equipment.model.Status;
import ph.edu.catc.labtrack.equipment.service.EquipmentService;
import ph.edu.catc.labtrack.profile.ProfileService;


@Configuration
public class DemoData {
    @Bean
    CommandLineRunner seed(JdbcTemplate jdbc, BCryptPasswordEncoder encoder, EquipmentService equipment, ProfileService profiles) {
        return args -> {
            // Seed only when empty, so restarting against a saved database doesn't fail on duplicates.
            Integer accounts = jdbc.queryForObject("select count(*) from app_user", Integer.class);
            if (accounts != null && accounts == 0) {
                String sql = "insert into app_user(username,password_hash,role) values (?,?,?)";
                jdbc.update(sql, "user", encoder.encode("user-demo"), "USER");
                jdbc.update(sql, "admin", encoder.encode("admin-demo"), "ADMIN");
            }

            if (equipment.list("").isEmpty()) {
                String lab1 = "Engineering Laboratory 1 | Room 301";
                String lab2 = "Engineering Laboratory 2 | Room 305";


                // Electrical Measurement and Testing
                equipment.create(new EquipmentRequest("CATC-mlt001DM", "Digital Multimeter",
                        "Measures voltage, current and resistance with auto-ranging.",
                        null, lab1, Status.AVAILABLE, 3));
                equipment.create(new EquipmentRequest("CATC-clm001CM", "Clamp Meter",
                        "Measures AC current without breaking the circuit.",
                        null, lab1, Status.AVAILABLE, 2));
                equipment.create(new EquipmentRequest("CATC-osc001DO", "Digital Oscilloscope",
                        "Displays and measures signal waveforms.",
                        null, lab1, Status.AVAILABLE, 2));
                equipment.create(new EquipmentRequest("CATC-psu001DP", "DC Power Supply",
                        "Adjustable bench supply for powering and testing circuits.",
                        null, lab2, Status.AVAILABLE, 3));
                equipment.create(new EquipmentRequest("CATC-fgn001FG", "Function Generator",
                        "Produces sine, square and triangle test signals.",
                        null, lab2, Status.AVAILABLE, 2));
                equipment.create(new EquipmentRequest("CATC-ins001IT", "Insulation Resistance Tester",
                        "Checks the insulation condition of wires and motors.",
                        null, lab2, Status.AVAILABLE, 1));
            }
            if (!profiles.exists("user")) {
                profiles.create("user", "S240000", "User N. Demo", "01234567891", "user@gmail.com",
                        "Bachelor of Science in Computer Engineering", "Year 3", null);
            }
            if (!profiles.exists("admin")) {
                profiles.create("admin", "EMP-0001", "Admin Custodian", "01234567890", "admin@gmail.com",
                        "Laboratory Custodian", "Staff", null);
            }
        };
    }
}
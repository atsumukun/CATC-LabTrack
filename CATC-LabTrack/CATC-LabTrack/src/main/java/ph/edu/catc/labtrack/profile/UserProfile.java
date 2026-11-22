package ph.edu.catc.labtrack.profile;

import jakarta.persistence.*;

@Entity
public class UserProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 64)
    private String username;
    @Column(nullable = false, length = 32)
    private String studentId;
    @Column(nullable = false, length = 80)
    private String fullName;
    @Column(length = 32)
    private String contactNumber;
    @Column(length = 120)
    private String email;
    @Column(nullable = false, length = 120)
    private String program;
    @Column(nullable = false, length = 30)
    private String yearLevel;
    @Column(length = 255)
    private String photoUrl;

    protected UserProfile() {}

    public UserProfile(String username, String studentId, String fullName, String contactNumber,
                       String email, String program, String yearLevel, String photoUrl) {
        this.username = username;
        this.studentId = studentId;
        this.fullName = fullName;
        this.contactNumber = contactNumber;
        this.email = email;
        this.program = program;
        this.yearLevel = yearLevel;
        this.photoUrl = photoUrl;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getStudentId() { return studentId; }
    public String getFullName() { return fullName; }
    public String getContactNumber() { return contactNumber; }
    public String getEmail() { return email; }
    public String getProgram() { return program; }
    public String getYearLevel() { return yearLevel; }
    public String getPhotoUrl() { return photoUrl; }
}
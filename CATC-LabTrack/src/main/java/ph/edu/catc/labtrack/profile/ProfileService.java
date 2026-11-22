package ph.edu.catc.labtrack.profile;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProfileService {
    private final UserProfileRepository repository;

    public ProfileService(UserProfileRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public Optional<ProfileResponse> find(String username) {
        return repository.findByUsername(username).map(ProfileResponse::from);
    }

    @Transactional(readOnly = true)
    public boolean exists(String username) {
        return repository.existsByUsername(username);
    }

    /** Used for the demo data now; the custodian's edit screen will use this later. */
    @Transactional
    public ProfileResponse create(String username, String studentId, String fullName, String contactNumber,
                                  String email, String program, String yearLevel, String photoUrl) {
        return ProfileResponse.from(repository.save(new UserProfile(username, studentId, fullName,
                contactNumber, email, program, yearLevel, photoUrl)));
    }
}
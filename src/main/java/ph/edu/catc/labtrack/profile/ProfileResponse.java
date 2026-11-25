package ph.edu.catc.labtrack.profile;

public record ProfileResponse(String username, String studentId, String fullName, String contactNumber,
                              String email, String program, String yearLevel, String photoUrl) {

    public static ProfileResponse from(UserProfile p) {
        return new ProfileResponse(p.getUsername(), p.getStudentId(), p.getFullName(), p.getContactNumber(),
                p.getEmail(), p.getProgram(), p.getYearLevel(), p.getPhotoUrl());
    }

    /** "User N. Demo" becomes "UD". Shown in the photo slot when there is no picture. */
    public String initials() {
        String[] parts = fullName.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase();
    }
}
package fr.epita.dto.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {
    private Long id;
    private String firstName;
    private String surname;
    private String email;
    private String role;
    private Long universityId;
}

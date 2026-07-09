package fr.epita.dto.Request;

import fr.epita.enums.LecturerStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CreateLecturerRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;
    
    private String email;

    @NotEmpty
    private List<Long> programmeIds;

    private String phone;

    private String password;

    private LecturerStatus status;
}

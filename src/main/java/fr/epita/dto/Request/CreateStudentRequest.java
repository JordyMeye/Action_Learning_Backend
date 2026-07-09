package fr.epita.dto.Request;

import fr.epita.enums.StudentStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateStudentRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @Email(message = "Must be a valid email address")
    private String email;

    @Email(message = "Must be a valid personal email address")
    private String personalEmail;

    private String password;

    @NotNull(message = "Programme ID is required")
    private Long programmeId;

    @NotNull(message = "Status is required")
    private StudentStatus status;

    @NotNull(message = "Cohort ID is required")
    private Long cohortId;
}



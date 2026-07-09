package fr.epita.dto.Request;

import fr.epita.enums.SubmissionStatus;
import fr.epita.enums.SubmissionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class CreateSubmissionRequest {

    @NotBlank
    private String title;

    private String description;

    private String additionalNotes;

    private Long courseId;

    private List<Long> courseIds;

    private Long lecturerId;

    @NotNull
    private LocalDate dueDate;

    private LocalTime dueTime;

    private SubmissionType submissionType;

    private SubmissionStatus status;

    @Min(1)
    private int maxPoints;

    @NotNull
    @Valid
    private SubmissionRulesRequest rules;

    private String templateFileName;

    private String instructions;
}

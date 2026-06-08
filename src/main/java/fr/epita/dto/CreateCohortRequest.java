package fr.epita.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCohortRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String programme;
}


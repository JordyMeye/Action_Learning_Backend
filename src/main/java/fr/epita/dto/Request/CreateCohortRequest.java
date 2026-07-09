package fr.epita.dto.Request;

import fr.epita.enums.CohortSeason;
import fr.epita.enums.CohortStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CreateCohortRequest {

    private String name;

    @NotNull
    private CohortSeason season;

    @NotNull
    private Integer academicYear;

    private CohortStatus status;

    private List<Long> programmeIds;
}

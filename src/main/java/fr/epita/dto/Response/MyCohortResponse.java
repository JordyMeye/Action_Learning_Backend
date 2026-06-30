package fr.epita.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyCohortResponse {
    private Long cohortId;
    private String cohortName;
    private String programmeName;
    private String programmeCode;
    private String status;
    private String universityName;
    private List<String> lecturerNames;
}

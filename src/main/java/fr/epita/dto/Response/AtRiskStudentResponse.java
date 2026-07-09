package fr.epita.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtRiskStudentResponse {
    private Long studentId;
    private String studentName;
    private String studentRef;
    private String cohortName;
    private String programmeName;
    private Double avgScorePct;
    private long gradedCount;
    private long missedSubmissions;
    private String reason;
}

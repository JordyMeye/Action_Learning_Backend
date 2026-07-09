package fr.epita.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantSummaryResponse {
    private long totalStudents;
    private long activeStudents;
    private long totalLecturers;
    private long activeLecturers;
    private long totalProgrammes;
    private long totalCohorts;
    private long activeCohorts;

    private long totalSubmissions;   
    private long releasedGrades;      
    private long gradedThisMonth;    
    private double avgScorePct;      
}

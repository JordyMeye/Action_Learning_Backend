package fr.epita.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CohortResponse {
    private Long id;
    private String name;
    private String programme;
    private String status;
}


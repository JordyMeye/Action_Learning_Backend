package fr.epita.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionRules {

    private String allowedFileTypes;

    private int maxAttempts;

    private boolean lateAllowed;
}

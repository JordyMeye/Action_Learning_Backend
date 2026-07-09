package fr.epita.dto.Request;

import fr.epita.enums.AnnouncementAudience;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SendAnnouncementRequest {

    @NotBlank
    private String subject;

    @NotBlank
    private String message;

    @NotNull
    private AnnouncementAudience audience;

    private Long cohortId;

    private List<Long> studentIds;

    private List<Long> lecturerIds;
}

package fr.epita.dto.Request;

import lombok.Data;

@Data
public class CreateProgrammeRequest {

    private String name;
    private String code;
    private String description;
}

package fr.epita.service;

import fr.epita.dto.Request.CreateProgrammeRequest;
import fr.epita.dto.Response.ProgrammeResponse;
import fr.epita.model.Programme;
import fr.epita.repository.ProgrammeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgrammeService {

    private final ProgrammeRepository programmeRepository;

    public ProgrammeResponse create(CreateProgrammeRequest request) {

        if (programmeRepository.existsByName(request.getName())) {
            throw new RuntimeException("Programme already exists");
        }

        Programme programme = Programme.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .build();

        return toResponse(programmeRepository.save(programme));
    }

    public List<ProgrammeResponse> getAll() {
        return programmeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProgrammeResponse getById(Long id) {
        Programme programme = programmeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Programme not found"));

        return toResponse(programme);
    }

    private ProgrammeResponse toResponse(Programme programme) {
        ProgrammeResponse response = new ProgrammeResponse();
        response.setId(programme.getId());
        response.setName(programme.getName());
        response.setCode(programme.getCode());
        response.setDescription(programme.getDescription());
        return response;
    }
}

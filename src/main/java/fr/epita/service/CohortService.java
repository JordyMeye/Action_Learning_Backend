package fr.epita.service;

import fr.epita.dto.CohortResponse;
import fr.epita.dto.CreateCohortRequest;
import fr.epita.model.Cohort;
import fr.epita.repository.CohortRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CohortService {

    private final CohortRepository cohortRepository;

    public List<CohortResponse> getAll() {
        return cohortRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CohortResponse create(CreateCohortRequest req) {
        Cohort cohort = Cohort.builder()
                .name(req.getName())
                .programme(req.getProgramme())
                .status("not_started")
                .archived(false)
                .build();

        return toResponse(cohortRepository.save(cohort));
    }

    @Transactional
    public CohortResponse update(Long id, CreateCohortRequest req) {
        Cohort cohort = cohortRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cohort not found"));

        cohort.setName(req.getName());
        cohort.setProgramme(req.getProgramme());

        return toResponse(cohortRepository.save(cohort));
    }

    @Transactional
    public void archive(Long id) {
        Cohort cohort = cohortRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cohort not found"));

        cohort.setArchived(true);
        cohort.setStatus("archived");

        cohortRepository.save(cohort);
    }

    private CohortResponse toResponse(Cohort c) {
        return CohortResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .programme(c.getProgramme())
                .status(c.getStatus())
                .build();
    }
}


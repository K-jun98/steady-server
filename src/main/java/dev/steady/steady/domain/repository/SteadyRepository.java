package dev.steady.steady.domain.repository;

import dev.steady.global.exception.NotFoundException;
import dev.steady.steady.domain.Steady;
import dev.steady.steady.exception.SteadyErrorCode;
import dev.steady.steady.infrastructure.SteadySearchRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import static dev.steady.steady.exception.SteadyErrorCode.*;

public interface SteadyRepository extends JpaRepository<Steady, Long>, SteadySearchRepository {

    default Steady getSteady(Long steadyId) {
        return findById(steadyId)
                .orElseThrow(() -> new NotFoundException(STEADY_NOT_FOUND));
    }

}

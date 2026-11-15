package com.homeshare.repository;

import com.homeshare.entity.RegraCasa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegraCasaRepository extends JpaRepository<RegraCasa, Long> {

    List<RegraCasa> findByCasaIdOrderByTituloAsc(Long casaId);

    Optional<RegraCasa> findByIdAndCasaId(Long id, Long casaId);
}

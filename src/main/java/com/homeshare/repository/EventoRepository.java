package com.homeshare.repository;

import com.homeshare.entity.Evento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    Optional<Evento> findByIdAndCasaId(Long id, Long casaId);

    @EntityGraph(attributePaths = {"criadoPor", "criadoPor.usuario"})
    List<Evento> findByCasaIdAndDataBetweenOrderByDataAscHorarioAsc(Long casaId, LocalDate de, LocalDate ate);
}

package com.homeshare.repository;

import com.homeshare.entity.Reserva;
import com.homeshare.enums.StatusReserva;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    boolean existsByEspacoId(Long espacoId);

    Optional<Reserva> findByIdAndEspacoCasaId(Long id, Long casaId);

    /** Quantas reservas no mesmo espaço se sobrepõem a [inicio, fim). Intervalos que apenas se tocam não contam. */
    @Query("""
            select count(r) from Reserva r
            where r.espaco.id = :espacoId
              and r.status = :status
              and r.inicio < :fim
              and r.fim > :inicio
            """)
    long contarConflitos(@Param("espacoId") Long espacoId,
                         @Param("status") StatusReserva status,
                         @Param("inicio") LocalDateTime inicio,
                         @Param("fim") LocalDateTime fim);

    @EntityGraph(attributePaths = {"espaco", "reservadoPor", "reservadoPor.usuario"})
    @Query("""
            select r from Reserva r
            where r.espaco.casa.id = :casaId
              and r.status = :status
              and (:espacoId is null or r.espaco.id = :espacoId)
              and r.fim > :de
              and r.inicio < :ate
            order by r.inicio asc, r.id asc
            """)
    List<Reserva> listar(@Param("casaId") Long casaId,
                         @Param("status") StatusReserva status,
                         @Param("espacoId") Long espacoId,
                         @Param("de") LocalDateTime de,
                         @Param("ate") LocalDateTime ate);
}

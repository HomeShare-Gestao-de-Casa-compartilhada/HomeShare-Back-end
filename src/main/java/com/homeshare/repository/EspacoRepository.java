package com.homeshare.repository;

import com.homeshare.entity.Espaco;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EspacoRepository extends JpaRepository<Espaco, Long> {

    List<Espaco> findByCasaIdOrderByNomeAsc(Long casaId);

    Optional<Espaco> findByIdAndCasaId(Long id, Long casaId);

    /** Trava o espaço enquanto uma reserva é criada: impede duas reservas simultâneas no mesmo horário. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from Espaco e where e.id = :id and e.casa.id = :casaId")
    Optional<Espaco> findByIdAndCasaIdParaAtualizar(@Param("id") Long id, @Param("casaId") Long casaId);
}

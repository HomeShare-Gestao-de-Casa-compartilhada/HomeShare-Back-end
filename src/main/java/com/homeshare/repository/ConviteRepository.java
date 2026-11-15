package com.homeshare.repository;

import com.homeshare.entity.Convite;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConviteRepository extends JpaRepository<Convite, Long> {

    boolean existsByCodigo(String codigo);

    Optional<Convite> findByCodigo(String codigo);

    /** Trava a linha: dois moradores usando o mesmo código ao mesmo tempo não passam juntos. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Convite c where c.codigo = :codigo")
    Optional<Convite> findByCodigoParaAtualizar(@Param("codigo") String codigo);

    Optional<Convite> findByIdAndCasaId(Long id, Long casaId);

    @EntityGraph(attributePaths = {"geradoPor", "geradoPor.usuario", "usadoPor", "usadoPor.usuario"})
    List<Convite> findByCasaIdOrderByDataCriacaoDesc(Long casaId);
}

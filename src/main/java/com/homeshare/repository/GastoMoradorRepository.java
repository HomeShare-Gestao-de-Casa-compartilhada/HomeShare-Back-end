package com.homeshare.repository;

import com.homeshare.entity.GastoMorador;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GastoMoradorRepository extends JpaRepository<GastoMorador, Long> {

    @EntityGraph(attributePaths = {"membro", "membro.usuario", "gastoFixo"})
    Optional<GastoMorador> findByIdAndGastoFixoCasaId(Long id, Long casaId);

    @Query("""
            select p from GastoMorador p
              join fetch p.gastoFixo g
            where g.casa.id = :casaId
              and p.membro.id = :membroId
            order by g.mesReferencia desc, g.id desc
            """)
    List<GastoMorador> findMinhas(@Param("casaId") Long casaId, @Param("membroId") Long membroId);
}

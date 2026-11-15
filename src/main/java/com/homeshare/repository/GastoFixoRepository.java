package com.homeshare.repository;

import com.homeshare.entity.GastoFixo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GastoFixoRepository extends JpaRepository<GastoFixo, Long> {

    Optional<GastoFixo> findByIdAndCasaId(Long id, Long casaId);

    @EntityGraph(attributePaths = {"parcelas", "parcelas.membro", "parcelas.membro.usuario"})
    List<GastoFixo> findByCasaIdOrderByMesReferenciaDescIdDesc(Long casaId);

    @EntityGraph(attributePaths = {"parcelas", "parcelas.membro", "parcelas.membro.usuario"})
    List<GastoFixo> findByCasaIdAndMesReferenciaOrderByIdDesc(Long casaId, LocalDate mesReferencia);
}

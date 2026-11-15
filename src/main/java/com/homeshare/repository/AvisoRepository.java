package com.homeshare.repository;

import com.homeshare.entity.Aviso;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AvisoRepository extends JpaRepository<Aviso, Long> {

    Optional<Aviso> findByIdAndCasaId(Long id, Long casaId);

    @EntityGraph(attributePaths = {"autor", "autor.usuario"})
    List<Aviso> findByCasaIdOrderByDataPublicacaoDescIdDesc(Long casaId);
}

package com.homeshare.repository;

import com.homeshare.entity.Contato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContatoRepository extends JpaRepository<Contato, Long> {

    List<Contato> findByCasaIdOrderByNomeAsc(Long casaId);

    Optional<Contato> findByIdAndCasaId(Long id, Long casaId);
}

package com.homeshare.repository;

import com.homeshare.entity.Tarefa;
import com.homeshare.enums.Prioridade;
import com.homeshare.enums.StatusTarefa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    Optional<Tarefa> findByIdAndCasaId(Long id, Long casaId);

    /** Filtros opcionais: passe null para ignorar. */
    @EntityGraph(attributePaths = {"responsavel", "responsavel.usuario", "concluidoPor", "concluidoPor.usuario"})
    @Query("""
            select t from Tarefa t
            where t.casa.id = :casaId
              and (:status is null or t.status = :status)
              and (:responsavelId is null or t.responsavel.id = :responsavelId)
              and (:prioridade is null or t.prioridade = :prioridade)
            order by t.prazo asc, t.id asc
            """)
    List<Tarefa> filtrar(@Param("casaId") Long casaId,
                         @Param("status") StatusTarefa status,
                         @Param("responsavelId") Long responsavelId,
                         @Param("prioridade") Prioridade prioridade);

    @EntityGraph(attributePaths = {"responsavel", "responsavel.usuario", "concluidoPor", "concluidoPor.usuario"})
    Page<Tarefa> findByCasaIdAndStatus(Long casaId, StatusTarefa status, Pageable pageable);
}

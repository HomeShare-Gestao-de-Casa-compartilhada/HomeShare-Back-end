package com.homeshare.repository;

import com.homeshare.entity.ItemFaltando;
import com.homeshare.enums.StatusItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemFaltandoRepository extends JpaRepository<ItemFaltando, Long> {

    Optional<ItemFaltando> findByIdAndCasaId(Long id, Long casaId);

    /** FALTANDO primeiro (ordem alfabética decrescente: "FALTANDO" > "COMPRADO"). */
    @EntityGraph(attributePaths = {"adicionadoPor", "adicionadoPor.usuario", "compradoPor", "compradoPor.usuario"})
    @Query("""
            select i from ItemFaltando i
            where i.casa.id = :casaId
              and (:status is null or i.status = :status)
            order by i.status desc, i.id desc
            """)
    List<ItemFaltando> filtrar(@Param("casaId") Long casaId, @Param("status") StatusItem status);
}

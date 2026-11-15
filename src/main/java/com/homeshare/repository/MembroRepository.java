package com.homeshare.repository;

import com.homeshare.entity.Membro;
import com.homeshare.enums.StatusCasa;
import com.homeshare.enums.StatusMembro;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MembroRepository extends JpaRepository<Membro, Long> {

    Optional<Membro> findByCasaIdAndUsuarioId(Long casaId, Long usuarioId);

    Optional<Membro> findByIdAndCasaId(Long id, Long casaId);

    @EntityGraph(attributePaths = "usuario")
    List<Membro> findByCasaIdAndStatusOrderByDataEntradaAscIdAsc(Long casaId, StatusMembro status);

    /** Sucessor da liderança: o morador ativo mais antigo (desempate por id), exceto quem está saindo. */
    Optional<Membro> findFirstByCasaIdAndStatusAndUsuarioIdNotOrderByDataEntradaAscIdAsc(
            Long casaId, StatusMembro status, Long usuarioId);

    @Query("""
            select m from Membro m
              join fetch m.casa c
              join fetch c.lider
            where m.usuario.id = :usuarioId
              and m.status = :statusMembro
              and c.status = :statusCasa
            order by c.nome
            """)
    List<Membro> findCasasDoUsuario(@Param("usuarioId") Long usuarioId,
                                    @Param("statusMembro") StatusMembro statusMembro,
                                    @Param("statusCasa") StatusCasa statusCasa);
}

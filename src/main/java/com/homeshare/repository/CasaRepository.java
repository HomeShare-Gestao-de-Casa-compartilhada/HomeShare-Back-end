package com.homeshare.repository;

import com.homeshare.entity.Casa;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CasaRepository extends JpaRepository<Casa, Long> {

    /** SELECT ... FOR UPDATE: serializa trocas de liderança, saídas e entradas na mesma casa. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Casa c where c.id = :id")
    Optional<Casa> findByIdParaAtualizar(@Param("id") Long id);
}

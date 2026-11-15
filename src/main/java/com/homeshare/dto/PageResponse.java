package com.homeshare.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Página simples e estável para o front (evita expor o PageImpl do Spring Data). */
public record PageResponse<T>(
        List<T> conteudo,
        int pagina,
        int tamanho,
        long totalElementos,
        int totalPaginas) {

    public static <E, T> PageResponse<T> de(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}

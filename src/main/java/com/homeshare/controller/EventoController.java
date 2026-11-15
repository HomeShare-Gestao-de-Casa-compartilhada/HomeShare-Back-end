package com.homeshare.controller;

import com.homeshare.dto.EventoDto;
import com.homeshare.security.SecurityUtils;
import com.homeshare.service.EventoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/casas/{casaId}/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventoDto.Response criar(@PathVariable Long casaId, @Valid @RequestBody EventoDto.Request req) {
        return eventoService.criar(casaId, SecurityUtils.usuarioId(), req);
    }

    /** Ex.: GET .../eventos?de=2026-11-01&ate=2026-11-30 */
    @GetMapping
    public List<EventoDto.Response> listar(
            @PathVariable Long casaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return eventoService.listar(casaId, SecurityUtils.usuarioId(), de, ate);
    }

    @PutMapping("/{eventoId}")
    public EventoDto.Response atualizar(@PathVariable Long casaId, @PathVariable Long eventoId,
                                        @Valid @RequestBody EventoDto.Request req) {
        return eventoService.atualizar(casaId, SecurityUtils.usuarioId(), eventoId, req);
    }

    @DeleteMapping("/{eventoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long casaId, @PathVariable Long eventoId) {
        eventoService.excluir(casaId, SecurityUtils.usuarioId(), eventoId);
    }
}

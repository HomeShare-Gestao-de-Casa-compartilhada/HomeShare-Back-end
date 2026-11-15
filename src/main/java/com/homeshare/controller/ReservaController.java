package com.homeshare.controller;

import com.homeshare.dto.ReservaDto;
import com.homeshare.security.SecurityUtils;
import com.homeshare.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping("/api/casas/{casaId}/espacos/{espacoId}/reservas")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservaDto.Response criar(@PathVariable Long casaId, @PathVariable Long espacoId,
                                     @Valid @RequestBody ReservaDto.Request req) {
        return reservaService.criar(casaId, SecurityUtils.usuarioId(), espacoId, req);
    }

    /** Ex.: GET .../reservas?espacoId=3&de=2026-11-20T00:00:00&ate=2026-11-27T23:59:00 */
    @GetMapping("/api/casas/{casaId}/reservas")
    public List<ReservaDto.Response> listar(
            @PathVariable Long casaId,
            @RequestParam(required = false) Long espacoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ate) {
        return reservaService.listar(casaId, SecurityUtils.usuarioId(), espacoId, de, ate);
    }

    @DeleteMapping("/api/casas/{casaId}/reservas/{reservaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Long casaId, @PathVariable Long reservaId) {
        reservaService.cancelar(casaId, SecurityUtils.usuarioId(), reservaId);
    }
}

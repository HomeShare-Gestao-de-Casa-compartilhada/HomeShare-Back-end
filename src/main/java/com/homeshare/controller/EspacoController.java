package com.homeshare.controller;

import com.homeshare.dto.EspacoDto;
import com.homeshare.security.SecurityUtils;
import com.homeshare.service.EspacoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/casas/{casaId}/espacos")
@RequiredArgsConstructor
public class EspacoController {

    private final EspacoService espacoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EspacoDto.Response criar(@PathVariable Long casaId, @Valid @RequestBody EspacoDto.Request req) {
        return espacoService.criar(casaId, SecurityUtils.usuarioId(), req);
    }

    @GetMapping
    public List<EspacoDto.Response> listar(@PathVariable Long casaId) {
        return espacoService.listar(casaId, SecurityUtils.usuarioId());
    }

    @PutMapping("/{espacoId}")
    public EspacoDto.Response atualizar(@PathVariable Long casaId, @PathVariable Long espacoId,
                                        @Valid @RequestBody EspacoDto.Request req) {
        return espacoService.atualizar(casaId, SecurityUtils.usuarioId(), espacoId, req);
    }

    @DeleteMapping("/{espacoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long casaId, @PathVariable Long espacoId) {
        espacoService.excluir(casaId, SecurityUtils.usuarioId(), espacoId);
    }
}

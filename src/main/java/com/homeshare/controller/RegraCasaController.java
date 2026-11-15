package com.homeshare.controller;

import com.homeshare.dto.RegraCasaDto;
import com.homeshare.security.SecurityUtils;
import com.homeshare.service.RegraCasaService;
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
@RequestMapping("/api/casas/{casaId}/regras")
@RequiredArgsConstructor
public class RegraCasaController {

    private final RegraCasaService regraService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegraCasaDto.Response criar(@PathVariable Long casaId, @Valid @RequestBody RegraCasaDto.Request req) {
        return regraService.criar(casaId, SecurityUtils.usuarioId(), req);
    }

    @GetMapping
    public List<RegraCasaDto.Response> listar(@PathVariable Long casaId) {
        return regraService.listar(casaId, SecurityUtils.usuarioId());
    }

    @PutMapping("/{regraId}")
    public RegraCasaDto.Response atualizar(@PathVariable Long casaId, @PathVariable Long regraId,
                                           @Valid @RequestBody RegraCasaDto.Request req) {
        return regraService.atualizar(casaId, SecurityUtils.usuarioId(), regraId, req);
    }

    @DeleteMapping("/{regraId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long casaId, @PathVariable Long regraId) {
        regraService.excluir(casaId, SecurityUtils.usuarioId(), regraId);
    }
}

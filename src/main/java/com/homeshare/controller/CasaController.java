package com.homeshare.controller;

import com.homeshare.dto.CasaDto;
import com.homeshare.dto.MembroDto;
import com.homeshare.security.SecurityUtils;
import com.homeshare.service.CasaService;
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
@RequestMapping("/api/casas")
@RequiredArgsConstructor
public class CasaController {

    private final CasaService casaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CasaDto.Response criar(@Valid @RequestBody CasaDto.Request req) {
        return casaService.criar(SecurityUtils.usuarioId(), req);
    }

    @GetMapping
    public List<CasaDto.Response> minhasCasas() {
        return casaService.minhasCasas(SecurityUtils.usuarioId());
    }

    @GetMapping("/{casaId}")
    public CasaDto.Response buscar(@PathVariable Long casaId) {
        return casaService.buscar(casaId, SecurityUtils.usuarioId());
    }

    @PutMapping("/{casaId}")
    public CasaDto.Response renomear(@PathVariable Long casaId, @Valid @RequestBody CasaDto.Request req) {
        return casaService.renomear(casaId, SecurityUtils.usuarioId(), req);
    }

    @GetMapping("/{casaId}/membros")
    public List<MembroDto.Response> membros(@PathVariable Long casaId) {
        return casaService.listarMembros(casaId, SecurityUtils.usuarioId());
    }

    @DeleteMapping("/{casaId}/membros/{membroId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void expulsar(@PathVariable Long casaId, @PathVariable Long membroId) {
        casaService.expulsar(casaId, SecurityUtils.usuarioId(), membroId);
    }

    @PostMapping("/{casaId}/sair")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sair(@PathVariable Long casaId) {
        casaService.sair(casaId, SecurityUtils.usuarioId());
    }

    @PutMapping("/{casaId}/lideranca")
    public CasaDto.Response transferirLideranca(@PathVariable Long casaId,
                                                @Valid @RequestBody CasaDto.TransferirLiderancaRequest req) {
        return casaService.transferirLideranca(casaId, SecurityUtils.usuarioId(), req.novoLiderMembroId());
    }
}

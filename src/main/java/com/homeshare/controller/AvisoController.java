package com.homeshare.controller;

import com.homeshare.dto.AvisoDto;
import com.homeshare.security.SecurityUtils;
import com.homeshare.service.AvisoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/casas/{casaId}/avisos")
@RequiredArgsConstructor
public class AvisoController {

    private final AvisoService avisoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AvisoDto.Response publicar(@PathVariable Long casaId, @Valid @RequestBody AvisoDto.Request req) {
        return avisoService.publicar(casaId, SecurityUtils.usuarioId(), req);
    }

    @GetMapping
    public List<AvisoDto.Response> listar(@PathVariable Long casaId) {
        return avisoService.listar(casaId, SecurityUtils.usuarioId());
    }

    @DeleteMapping("/{avisoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long casaId, @PathVariable Long avisoId) {
        avisoService.excluir(casaId, SecurityUtils.usuarioId(), avisoId);
    }
}

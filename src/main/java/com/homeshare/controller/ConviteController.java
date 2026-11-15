package com.homeshare.controller;

import com.homeshare.dto.CasaDto;
import com.homeshare.dto.ConviteDto;
import com.homeshare.security.SecurityUtils;
import com.homeshare.service.ConviteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ConviteController {

    private final ConviteService conviteService;

    // ----- líder -----

    /** Corpo opcional: {"validadeHoras": 24}. Pode enviar vazio ({}). */
    @PostMapping("/api/casas/{casaId}/convites")
    @ResponseStatus(HttpStatus.CREATED)
    public ConviteDto.Response criar(@PathVariable Long casaId,
                                     @Valid @RequestBody(required = false) ConviteDto.CriarRequest req) {
        return conviteService.criar(casaId, SecurityUtils.usuarioId(), req);
    }

    @GetMapping("/api/casas/{casaId}/convites")
    public List<ConviteDto.Response> listar(@PathVariable Long casaId) {
        return conviteService.listar(casaId, SecurityUtils.usuarioId());
    }

    @DeleteMapping("/api/casas/{casaId}/convites/{conviteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Long casaId, @PathVariable Long conviteId) {
        conviteService.cancelar(casaId, SecurityUtils.usuarioId(), conviteId);
    }

    // ----- quem recebeu o código -----

    @GetMapping("/api/convites/{codigo}")
    public ConviteDto.PreviaResponse previa(@PathVariable String codigo) {
        return conviteService.previa(codigo);
    }

    @PostMapping("/api/convites/{codigo}/entrar")
    public CasaDto.Response entrar(@PathVariable String codigo) {
        return conviteService.entrar(SecurityUtils.usuarioId(), codigo);
    }
}

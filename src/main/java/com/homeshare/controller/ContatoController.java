package com.homeshare.controller;

import com.homeshare.dto.ContatoDto;
import com.homeshare.security.SecurityUtils;
import com.homeshare.service.ContatoService;
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
@RequestMapping("/api/casas/{casaId}/contatos")
@RequiredArgsConstructor
public class ContatoController {

    private final ContatoService contatoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContatoDto.Response criar(@PathVariable Long casaId, @Valid @RequestBody ContatoDto.Request req) {
        return contatoService.criar(casaId, SecurityUtils.usuarioId(), req);
    }

    @GetMapping
    public List<ContatoDto.Response> listar(@PathVariable Long casaId) {
        return contatoService.listar(casaId, SecurityUtils.usuarioId());
    }

    @PutMapping("/{contatoId}")
    public ContatoDto.Response atualizar(@PathVariable Long casaId, @PathVariable Long contatoId,
                                         @Valid @RequestBody ContatoDto.Request req) {
        return contatoService.atualizar(casaId, SecurityUtils.usuarioId(), contatoId, req);
    }

    @DeleteMapping("/{contatoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long casaId, @PathVariable Long contatoId) {
        contatoService.excluir(casaId, SecurityUtils.usuarioId(), contatoId);
    }
}

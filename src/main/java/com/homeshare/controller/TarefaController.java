package com.homeshare.controller;

import com.homeshare.dto.PageResponse;
import com.homeshare.dto.TarefaDto;
import com.homeshare.enums.Prioridade;
import com.homeshare.enums.StatusTarefa;
import com.homeshare.security.SecurityUtils;
import com.homeshare.service.TarefaService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/casas/{casaId}/tarefas")
@RequiredArgsConstructor
public class TarefaController {

    private final TarefaService tarefaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TarefaDto.Response criar(@PathVariable Long casaId, @Valid @RequestBody TarefaDto.Request req) {
        return tarefaService.criar(casaId, SecurityUtils.usuarioId(), req);
    }

    @GetMapping
    public List<TarefaDto.Response> listar(@PathVariable Long casaId,
                                           @RequestParam(required = false) StatusTarefa status,
                                           @RequestParam(required = false) Long responsavelId,
                                           @RequestParam(required = false) Prioridade prioridade) {
        return tarefaService.listar(casaId, SecurityUtils.usuarioId(), status, responsavelId, prioridade);
    }

    @GetMapping("/minhas")
    public List<TarefaDto.Response> minhas(@PathVariable Long casaId,
                                           @RequestParam(required = false) StatusTarefa status) {
        return tarefaService.minhas(casaId, SecurityUtils.usuarioId(), status);
    }

    @GetMapping("/historico")
    public PageResponse<TarefaDto.Response> historico(@PathVariable Long casaId,
                                                      @RequestParam(defaultValue = "0") int pagina,
                                                      @RequestParam(defaultValue = "20") int tamanho) {
        return tarefaService.historico(casaId, SecurityUtils.usuarioId(), pagina, tamanho);
    }

    @PutMapping("/{tarefaId}")
    public TarefaDto.Response atualizar(@PathVariable Long casaId, @PathVariable Long tarefaId,
                                        @Valid @RequestBody TarefaDto.Request req) {
        return tarefaService.atualizar(casaId, SecurityUtils.usuarioId(), tarefaId, req);
    }

    @PostMapping("/{tarefaId}/concluir")
    public TarefaDto.Response concluir(@PathVariable Long casaId, @PathVariable Long tarefaId) {
        return tarefaService.concluir(casaId, SecurityUtils.usuarioId(), tarefaId);
    }

    @DeleteMapping("/{tarefaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Long casaId, @PathVariable Long tarefaId) {
        tarefaService.cancelar(casaId, SecurityUtils.usuarioId(), tarefaId);
    }
}

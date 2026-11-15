package com.homeshare.controller;

import com.homeshare.dto.ItemFaltandoDto;
import com.homeshare.enums.StatusItem;
import com.homeshare.security.SecurityUtils;
import com.homeshare.service.ItemFaltandoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/casas/{casaId}/itens")
@RequiredArgsConstructor
public class ItemFaltandoController {

    private final ItemFaltandoService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemFaltandoDto.Response adicionar(@PathVariable Long casaId,
                                              @Valid @RequestBody ItemFaltandoDto.Request req) {
        return itemService.adicionar(casaId, SecurityUtils.usuarioId(), req);
    }

    @GetMapping
    public List<ItemFaltandoDto.Response> listar(@PathVariable Long casaId,
                                                 @RequestParam(required = false) StatusItem status) {
        return itemService.listar(casaId, SecurityUtils.usuarioId(), status);
    }

    @PostMapping("/{itemId}/comprar")
    public ItemFaltandoDto.Response comprar(@PathVariable Long casaId, @PathVariable Long itemId) {
        return itemService.marcarComprado(casaId, SecurityUtils.usuarioId(), itemId);
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long casaId, @PathVariable Long itemId) {
        itemService.excluir(casaId, SecurityUtils.usuarioId(), itemId);
    }
}

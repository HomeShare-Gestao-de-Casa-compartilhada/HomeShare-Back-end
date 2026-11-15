package com.homeshare.controller;

import com.homeshare.dto.GastoDto;
import com.homeshare.security.SecurityUtils;
import com.homeshare.service.GastoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/casas/{casaId}/gastos")
@RequiredArgsConstructor
public class GastoController {

    private final GastoService gastoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GastoDto.Response criar(@PathVariable Long casaId, @Valid @RequestBody GastoDto.CriarRequest req) {
        return gastoService.criar(casaId, SecurityUtils.usuarioId(), req);
    }

    /** Ex.: GET .../gastos?mes=2026-11 */
    @GetMapping
    public List<GastoDto.Response> listar(@PathVariable Long casaId,
                                          @RequestParam(required = false) String mes) {
        return gastoService.listar(casaId, SecurityUtils.usuarioId(), mes);
    }

    @GetMapping("/minhas-parcelas")
    public List<GastoDto.MinhaParcelaResponse> minhasParcelas(@PathVariable Long casaId) {
        return gastoService.minhasParcelas(casaId, SecurityUtils.usuarioId());
    }

    /** A "checkbox" de pago: {"pago": true}. */
    @PatchMapping("/parcelas/{parcelaId}/pagamento")
    public GastoDto.ParcelaResponse pagamento(@PathVariable Long casaId, @PathVariable Long parcelaId,
                                              @Valid @RequestBody GastoDto.PagamentoRequest req) {
        return gastoService.marcarPagamento(casaId, SecurityUtils.usuarioId(), parcelaId, req.pago());
    }

    @DeleteMapping("/{gastoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long casaId, @PathVariable Long gastoId) {
        gastoService.excluir(casaId, SecurityUtils.usuarioId(), gastoId);
    }
}

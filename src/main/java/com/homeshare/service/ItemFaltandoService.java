package com.homeshare.service;

import com.homeshare.dto.ItemFaltandoDto;
import com.homeshare.entity.ItemFaltando;
import com.homeshare.entity.Membro;
import com.homeshare.enums.StatusItem;
import com.homeshare.exception.BusinessException;
import com.homeshare.repository.ItemFaltandoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/** Lista do que falta na casa. */
@Service
@RequiredArgsConstructor
public class ItemFaltandoService {

    private final ItemFaltandoRepository itemRepository;
    private final CasaAcessoService acesso;
    private final Clock clock;

    @Transactional
    public ItemFaltandoDto.Response adicionar(Long casaId, Long usuarioId, ItemFaltandoDto.Request req) {
        Membro ator = acesso.exigirMembroAtivo(casaId, usuarioId);
        ItemFaltando item = new ItemFaltando();
        item.setNome(req.nome().trim());
        item.setQuantidade(req.quantidade());
        item.setStatus(StatusItem.FALTANDO);
        item.setAdicionadoPor(ator);
        item.setCasa(ator.getCasa());
        itemRepository.save(item);
        return ItemFaltandoDto.Response.de(item);
    }

    @Transactional(readOnly = true)
    public List<ItemFaltandoDto.Response> listar(Long casaId, Long usuarioId, StatusItem status) {
        acesso.exigirMembroAtivo(casaId, usuarioId);
        return itemRepository.filtrar(casaId, status).stream()
                .map(ItemFaltandoDto.Response::de)
                .toList();
    }

    @Transactional
    public ItemFaltandoDto.Response marcarComprado(Long casaId, Long usuarioId, Long itemId) {
        Membro ator = acesso.exigirMembroAtivo(casaId, usuarioId);
        ItemFaltando item = buscar(itemId, casaId);
        if (item.getStatus() == StatusItem.COMPRADO) {
            throw BusinessException.conflict("Este item já foi marcado como comprado.");
        }
        item.setStatus(StatusItem.COMPRADO);
        item.setCompradoPor(ator);
        item.setDataCompra(LocalDateTime.now(clock));
        return ItemFaltandoDto.Response.de(item);
    }

    /** Quem adicionou ou o líder podem remover. */
    @Transactional
    public void excluir(Long casaId, Long usuarioId, Long itemId) {
        Membro ator = acesso.exigirMembroAtivo(casaId, usuarioId);
        ItemFaltando item = buscar(itemId, casaId);
        boolean autor = item.getAdicionadoPor().getId().equals(ator.getId());
        if (!autor && !acesso.ehLider(ator)) {
            throw BusinessException.forbidden("Apenas quem adicionou o item ou o líder pode removê-lo.");
        }
        itemRepository.delete(item);
    }

    private ItemFaltando buscar(Long itemId, Long casaId) {
        return itemRepository.findByIdAndCasaId(itemId, casaId)
                .orElseThrow(() -> BusinessException.notFound("Item não encontrado."));
    }
}
